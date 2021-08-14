package com.bolingcavalry.relatedoperation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bolingcavalry.relatedoperation.mapper")
public class RelatedOperationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelatedOperationApplication.class, args);
    }

}
