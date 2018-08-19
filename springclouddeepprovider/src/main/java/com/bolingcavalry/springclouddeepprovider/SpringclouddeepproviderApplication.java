package com.bolingcavalry.springclouddeepprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SpringclouddeepproviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringclouddeepproviderApplication.class, args);
	}
}
