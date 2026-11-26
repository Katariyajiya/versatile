package com.versatile.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
class HealthController{

    @GetMapping
    public String getHealth(){
        return "system is active and healthy";
    }
}