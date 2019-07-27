package com.bolingcavalry.springcloudk8sreloadconfigdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;


@SpringBootApplication
@RestController
@EnableConfigurationProperties(DummyConfig.class)
public class Springcloudk8sreloadconfigdemoApplication {

    @Autowired
    private DummyConfig dummyConfig;

    @GetMapping("/health")
    public String health() {
        return "success";
    }

    @GetMapping("/hello")
    public String hello() {
        return dummyConfig.getMessage()
                + " ["
                + new SimpleDateFormat().format(new Date())
                + "]";
    }

    public static void main(String[] args) {
        SpringApplication.run(Springcloudk8sreloadconfigdemoApplication.class, args);
    }

}
