package com.fyp.AABookingProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AaBookingProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(AaBookingProjectApplication.class, args);
	}

}
