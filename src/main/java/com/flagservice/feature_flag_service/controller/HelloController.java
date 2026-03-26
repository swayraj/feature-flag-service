package com.flagservice.feature_flag_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "Service is healthy!";
    }

    @GetMapping({"/app", "/app/"})
    public String app() {
        return "forward:/app/index.html";
    }

}
