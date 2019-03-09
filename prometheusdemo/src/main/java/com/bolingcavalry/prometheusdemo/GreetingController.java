package com.bolingcavalry.prometheusdemo;

import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
class GreetingController {

    @Autowired
    Counter counter;

    @GetMapping("/greet")
    String greet(@RequestParam(defaultValue = "World") String name) {
        counter.increment();
        return "Hello: " + name + " " + LocalDateTime.now();
    }
}
