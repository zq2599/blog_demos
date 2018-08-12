package com.bolingcavalry.springcustomize;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

@SpringBootApplication
public class SpringcustomizeApplication {

    public static void main(String[] args) {
        AnnotationConfigServletWebServerApplicationContext a;
        SpringApplication.run(SpringcustomizeApplication.class, args);
    }
}
