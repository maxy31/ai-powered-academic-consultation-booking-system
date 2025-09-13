package com.fyp.AABookingProject.profile.service;

import com.fyp.AABookingProject.core.entity.Advisor;
import com.fyp.AABookingProject.core.entity.Department;
import com.fyp.AABookingProject.core.entity.Student;
import com.fyp.AABookingProject.core.entity.User;
import com.fyp.AABookingProject.core.repository.AdvisorRepository;
import com.fyp.AABookingProject.core.repository.DepartmentRepository;
import com.fyp.AABookingProject.core.repository.StudentRepository;
import com.fyp.AABookingProject.core.repository.UserRepository;
import com.fyp.AABookingProject.profile.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ProfileService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AdvisorRepository advisorRepository;

    @Autowired
    DepartmentRepository departmentRepository;

    @Autowired
    StudentRepository studentRepository;

//    Student Profile Service
    public StudentProfileResponse getStudentProfile(){
        UserDetails userDetails = getUserDetails();
        Optional<User> userTarget =  userRepository.findByUsername(userDetails.getUsername());
        StudentProfileResponse studentProfileResponse = new StudentProfileResponse();

        if(userTarget.isPresent()){
            User user = userTarget.get();

            if(user.getRole().getCode().equals("S")){
                studentProfileResponse.setStudentName(user.getFirstName() + " " + user.getLastName());
                studentProfileResponse.setUsername(user.getUsername());
                studentProfileResponse.setPhoneNumber(user.getPhone());
                studentProfileResponse.setEmail(user.getEmail());
                Student student = user.getStudent();

                studentProfileResponse.setAdvisorName(student.getAdvisor().getUser().getFirstName() + " " +student.getAdvisor().getUser().getLastName());
            } else {
                throw new RuntimeException("Wrong Role Assign,");
            }
        } else {
            throw new IllegalArgumentException("User Not Found.");
        }
        return studentProfileResponse;
    }

    public EditProfileStudentResponse editStudentProfile(EditProfileStudentRequest editProfileStudentRequest){
        UserDetails userDetails = getUserDetails();
        Optional<User> userTarget = userRepository.findByUsername(userDetails.getUsername());
        EditProfileStudentResponse response = new EditProfileStudentResponse();

        if(userTarget.isPresent()){
            User user = userTarget.get();
            user.setFirstName(editProfileStudentRequest.getFirstName());
            user.setLastName(editProfileStudentRequest.getLastName());
            user.setPhone(editProfileStudentRequest.getPhoneNumber());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            response.setUsername(user.getUsername());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setPhoneNumber(user.getPhone());
        } else {
            throw new IllegalArgumentException("Username Not Found.");
        }

        return response;
    }

//    Advisor Profile Service
    public AdvisorProfileResponse getAdvisorProfile(){
        UserDetails userDetails = getUserDetails();
        Optional<User> userTarget =  userRepository.findByUsername(userDetails.getUsername());
        AdvisorProfileResponse advisorProfileResponse = new AdvisorProfileResponse();

        if(userTarget.isPresent()){
            User user = userTarget.get();

            if(user.getRole().getCode().equals("A")){
                advisorProfileResponse.setAdvisorName(user.getFirstName() + " " + user.getLastName());
                advisorProfileResponse.setUsername(user.getUsername());
                advisorProfileResponse.setPhoneNumber(user.getPhone());
                advisorProfileResponse.setEmail(user.getEmail());

                Advisor advisor = user.getAdvisor();
                Optional<Department> departmentTarget = departmentRepository.findById(advisor.getDepartmentId());

                departmentTarget.ifPresent(department -> advisorProfileResponse.setDepartment(department.getDepartmentName()));
            } else {
                throw new RuntimeException("Wrong Role Assign,");
            }
        } else {
            throw new IllegalArgumentException("User Not Found.");
        }
        return advisorProfileResponse;
    }

    public EditProfileAdvisorResponse editAdvisorProfile(EditProfileAdvisorRequest editProfileAdvisorRequest){
        UserDetails userDetails = getUserDetails();
        Optional<User> userTarget = userRepository.findByUsername(userDetails.getUsername());
        EditProfileAdvisorResponse response = new EditProfileAdvisorResponse();

        if(userTarget.isPresent()){
            User user = userTarget.get();
            user.setFirstName(editProfileAdvisorRequest.getFirstName());
            user.setLastName(editProfileAdvisorRequest.getLastName());
            user.setPhone(editProfileAdvisorRequest.getPhoneNumber());
            user.setUpdatedAt(LocalDateTime.now());

            Advisor advisor = user.getAdvisor();

            advisor.setDepartmentId(editProfileAdvisorRequest.getDepartmentId());

            userRepository.save(user);

            response.setUsername(user.getUsername());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setPhoneNumber(user.getPhone());
            response.setDepartmentId(user.getAdvisor().getDepartmentId());
        } else {
            throw new IllegalArgumentException("Username Not Found.");
        }

        return response;
    }

    private UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return (UserDetails) authentication.getPrincipal();
    }
}
