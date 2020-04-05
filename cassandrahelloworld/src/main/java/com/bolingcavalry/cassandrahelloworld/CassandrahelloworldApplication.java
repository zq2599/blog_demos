package com.bolingcavalry.cassandrahelloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CassandrahelloworldApplication {

    public static void main(String[] args) {
        SpringApplication.run(CassandrahelloworldApplication.class, args);
    }

}
