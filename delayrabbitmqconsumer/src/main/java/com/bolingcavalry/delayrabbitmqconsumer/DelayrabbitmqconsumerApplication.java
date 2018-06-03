package com.bolingcavalry.delayrabbitmqconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DelayrabbitmqconsumerApplication {

	public static void main(String[] args) {
		try{
			Thread.sleep(6000);
		}catch(Exception e){
			e.printStackTrace();
		}
		SpringApplication.run(DelayrabbitmqconsumerApplication.class, args);
	}
}
