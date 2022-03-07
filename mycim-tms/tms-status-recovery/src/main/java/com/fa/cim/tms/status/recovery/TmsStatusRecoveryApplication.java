package com.fa.cim.tms.status.recovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
@ComponentScan("com.fa.cim")
public class TmsStatusRecoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmsStatusRecoveryApplication.class, args);
    }
}
