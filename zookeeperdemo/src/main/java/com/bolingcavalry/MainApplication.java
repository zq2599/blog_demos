package com.bolingcavalry;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * 入口
 * @author bolingcavalry
 * @email zq2599@gmail.com
 * @date 2017/04/09 11:29
 */
@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(MainApplication.class).web(true).run(args);
    }

}
