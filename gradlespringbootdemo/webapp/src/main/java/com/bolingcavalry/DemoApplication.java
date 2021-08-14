package com.bolingcavalry;

import com.bolingcavalry.dto.Student;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@SpringBootApplication
@RestController
public class DemoApplication {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {
        return "Hello "
                + new Date()
                + "--"
                + new Student("Tom", 11);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}