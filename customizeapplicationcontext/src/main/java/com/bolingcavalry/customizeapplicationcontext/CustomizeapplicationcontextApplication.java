package com.bolingcavalry.customizeapplicationcontext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CustomizeapplicationcontextApplication {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(CustomizeapplicationcontextApplication.class);
		springApplication.setApplicationContextClass(CustomizeApplicationContext.class);
		springApplication.run(args);
	}
}
