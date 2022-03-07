package com.fa.cim.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;




@EnableDiscoveryClient
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = "com.fa.cim")
public class MycimMcsApplication {
	public static void main(String[] args) {
		SpringApplication.run(MycimMcsApplication.class, args);
	}


}
