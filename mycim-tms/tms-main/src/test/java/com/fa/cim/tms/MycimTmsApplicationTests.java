package com.fa.cim.tms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableDiscoveryClient
public class MycimTmsApplicationTests {
	public static void main(String[] args) {
		SpringApplication.run(MycimTmsApplication.class, args);
	}
	@Test
	public void contextLoads() {
	}

}
