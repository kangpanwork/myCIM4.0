package com.fa.cim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class SentinelMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelMailApplication.class, args);
    }
}
