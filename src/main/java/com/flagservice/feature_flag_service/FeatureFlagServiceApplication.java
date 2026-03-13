package com.flagservice.feature_flag_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class FeatureFlagServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeatureFlagServiceApplication.class, args);
    }

}