package com.ascs.tech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TechApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechApplication.class, args);
    }
}
