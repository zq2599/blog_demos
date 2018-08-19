package com.bolingcavalry.springclouddeepeureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class SpringclouddeepeurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringclouddeepeurekaApplication.class, args);
	}
}
