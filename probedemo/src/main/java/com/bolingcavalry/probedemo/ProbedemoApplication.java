package com.bolingcavalry.probedemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

@SpringBootApplication(scanBasePackages = "com.bolingcavalry.probedemo")
public class ProbedemoApplication {

    @Resource
    ApplicationAvailability applicationAvailability;


    public static void main(String[] args) {
        SpringApplication.run(ProbedemoApplication.class, args);
    }

    @RequestMapping(value="/get1")
    public String state(){
        return "livenessState : " + applicationAvailability.getLivenessState()
                + "<br>readinessState : " + applicationAvailability.getReadinessState()
                + "<br>" + new Date();
    }

}
