package com.flagservice.feature_flag_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FeatureFlagServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeatureFlagServiceApplication.class, args);
	}

}
