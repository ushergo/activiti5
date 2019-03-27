package com.example.activitifive;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication(scanBasePackages = "com.example.activitifive",exclude = SecurityAutoConfiguration.class)
public class ActivitifiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActivitifiveApplication.class, args);
    }

}
