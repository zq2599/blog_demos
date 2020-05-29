package com.bolingcavalry.dockerlayerdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@SpringBootApplication
@RestController
public class DockerlayerdemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DockerlayerdemoApplication.class, args);
	}


	@RequestMapping(value = "/hello")
	public String hello(){
		return "hello " + new Date();
	}
}
