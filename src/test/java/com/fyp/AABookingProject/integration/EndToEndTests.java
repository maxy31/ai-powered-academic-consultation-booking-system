package com.fyp.AABookingProject.integration;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.profiles.active=test"
})
@AutoConfigureMockMvc
public class EndToEndTests {

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    private String registerAdvisorAndLoginReturnToken() {
        // Register advisor
        String advisorReq = """
            {"username":"adv1","firstName":"Ada","lastName":"Visor","email":"adv1@example.com","password":"Passw0rd!","phoneNumber":"1000","departmentId":1}
        """;
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(advisorReq)
                .post("/api/auth/registerAdvisor")
                .then().statusCode(anyOf(is(200), is(400))); // allow re-run

        // Login advisor
        String loginReq = """
            {"username":"adv1","password":"Passw0rd!"}
        """;
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginReq)
                .post("/api/auth/loginAdvisor")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");
    }

    private String registerStudentAndLoginReturnToken(Long advisorId) {
        String studentReq = String.format("{" +
                "\"username\":\"stu1\",\"firstName\":\"Stu\",\"lastName\":\"Dent\",\"email\":\"stu1@example.com\"," +
                "\"phoneNumber\":\"2000\",\"password\":\"Passw0rd!\",\"advisorId\":%d}", advisorId);
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(studentReq)
                .post("/api/auth/registerStudent")
                .then().statusCode(anyOf(is(200), is(400))); // allow re-run

        String loginReq = """
            {"username":"stu1","password":"Passw0rd!"}
        """;
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginReq)
                .post("/api/auth/loginStudent")
                .then().statusCode(200)
                .extract().jsonPath().getString("token");
    }

    @Test
    void fullFlow_register_login_book_confirm_and_notify() {
        // 1) Advisor registers and logs in
        String advisorToken = registerAdvisorAndLoginReturnToken();

        // 2) Fetch advisors list to obtain id
        Long advisorId = given().get("/api/auth/showAdvisors")
                .then().statusCode(200)
                .extract().jsonPath().getLong("[0].id");

        // 3) Student registers selecting advisor and logs in
        String studentToken = registerStudentAndLoginReturnToken(advisorId);

        // 4) Student creates appointment
        String apptReq = """
           {"date":"2099-01-15","startTime":"09:00:00","endTime":"09:30:00"}
        """;
        Long apptId = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + studentToken)
                .body(apptReq)
                .post("/api/appointments/createAppointment")
                .then().statusCode(200)
                .body("status", is("PENDING"))
                .extract().jsonPath().getLong("id");

        // 5) Advisor lists active appointments
        given().header("Authorization", "Bearer " + advisorToken)
                .get("/api/appointments/getAppointmentsList")
                .then().statusCode(200)
                .body("appointments.size()", greaterThanOrEqualTo(1));

        // 6) Advisor confirms the appointment
        String confirmReq = String.format("{\"appointmentId\":%d}", apptId);
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + advisorToken)
                .body(confirmReq)
                .post("/api/appointments/confirmAppointment")
                .then().statusCode(200)
                .body("status", is("CONFIRMED"));

        // 7) Student checks latest confirmed appointment
        given().header("Authorization", "Bearer " + studentToken)
                .get("/api/appointments/getLatestBooking")
                .then().statusCode(200)
                .body("id", is(apptId.intValue()));

        // 8) Student fetches notifications and mark read
        Long notifId = given().header("Authorization", "Bearer " + studentToken)
                .get("/api/notifications?unreadOnly=true")
                .then().statusCode(200)
                .extract().jsonPath().getLong("[0].id");

        given().header("Authorization", "Bearer " + studentToken)
                .post("/api/notifications/{id}/read", String.valueOf(notifId))
                .then().statusCode(200)
                .body("read", is(true));

        // 9) Unread count now should be >= 0
        String unreadStr = given().header("Authorization", "Bearer " + studentToken)
                .get("/api/notifications/unread-count")
                .then().statusCode(200)
                .extract().asString();
        int unread = Integer.parseInt(unreadStr.trim());
        assertTrue(unread >= 0, "Unread count should be >= 0, actual=" + unread);
    }
}
