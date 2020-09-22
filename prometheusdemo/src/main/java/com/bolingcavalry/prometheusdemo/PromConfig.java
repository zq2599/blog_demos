package com.bolingcavalry.prometheusdemo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PromConfig {

    @Autowired
    PrometheusMeterRegistry registry;

    @Bean
    public Counter getCounter() {
        Counter counter = Counter.builder("my_sample_counter")
                .tags("status", "success")
                .description("A simple Counter to illustrate custom Counters in Spring Boot and Prometheus")
                .register(registry);
        return counter;
    }
}
