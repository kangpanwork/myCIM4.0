package com.fa.cim.controller.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthStatus {

    @ResponseBody
    @GetMapping(value = "/status")
    public Map<String,String> status() {
        Map<String, String> result = new HashMap<>();
        result.put("status", "UP");
        return result;
    }
}