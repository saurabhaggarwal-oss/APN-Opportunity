package com.ttn.ck.apn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@ComponentScan(basePackages = {"com.ttn.ck"})
@SpringBootApplication
public class ApnOpportunityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApnOpportunityApplication.class, args);
	}

}
