package com.fa.cim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
public class SentinelHistoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelHistoryApplication.class, args);
    }
}
