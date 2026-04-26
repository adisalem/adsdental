package com.cs489.project.adsdentalapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to ADS Dental Application");
        response.put("version", "0.0.1");
        response.put("status", "Running");
        response.put("graphql_endpoint", "/graphql");
        response.put("h2_console", "/h2-console");
        return ResponseEntity.ok(response);
    }
}
