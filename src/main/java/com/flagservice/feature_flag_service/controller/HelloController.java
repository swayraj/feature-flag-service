package com.flagservice.feature_flag_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello()
    {
        return "Feature Flag Service is RUNNING! Welcome to Day 1!";
    }

    @GetMapping("/health")
    public String health() {
        return "Service is healthy!";
    }

}
