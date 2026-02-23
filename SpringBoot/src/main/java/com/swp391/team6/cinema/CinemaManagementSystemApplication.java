package com.swp391.team6.cinema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CinemaManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CinemaManagementSystemApplication.class);

        app.run(args);
    }
}