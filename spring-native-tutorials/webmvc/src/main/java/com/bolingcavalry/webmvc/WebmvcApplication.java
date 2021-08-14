package com.bolingcavalry.webmvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@SpringBootApplication
@RestController
public class WebmvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebmvcApplication.class, args);
	}

	@ResponseStatus(HttpStatus.ACCEPTED)
	@GetMapping("/status")
	public String status() {
		return "status";
	}

	@GetMapping("/")
	public String hello() {
		return "2. Hello from Spring MVC and Tomcat, " + LocalDateTime.now();
	}
}
