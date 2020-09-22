package com.bolingcavalry.druidonesource;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bolingcavalry.druidonesource.mapper")
public class DuridOneSourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuridOneSourceApplication.class, args);
    }

}
