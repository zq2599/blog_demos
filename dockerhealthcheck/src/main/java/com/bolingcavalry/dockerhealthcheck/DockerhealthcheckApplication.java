package com.bolingcavalry.dockerhealthcheck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
@RestController
@Slf4j
public class DockerhealthcheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerhealthcheckApplication.class, args);
    }

    /**
     * 表示当前业务容器是否健康
     */
    private boolean isHealth = true;

    @RequestMapping(value = "/setstate", method = RequestMethod.GET)
    public String setState(@RequestParam(name="state") boolean state) {
        isHealth = state;
        return "set state success [" + isHealth + "]";
    }

    @RequestMapping(value = "/getstate", method = RequestMethod.GET)
    public ResponseEntity<String> getState(){
        if(isHealth) {
            log.info("step probe return success");
            return ResponseEntity.status(200).build();
        } else {
            log.info("step probe return fail");
            return ResponseEntity.status(403).build();
        }
    }

}
