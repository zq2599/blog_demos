package com.bolingcavalry;

import com.bolingcavalry.dao.AdvancedEtcdService;
import com.bolingcavalry.dao.EtcdService;
import com.bolingcavalry.dao.impl.AdvancedEtcdServiceImpl;
import com.bolingcavalry.dao.impl.EtcdServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@SpringBootApplication
public class AdvancedOperateApplication {

    private static final String IP = "192.168.133.218";

    public static final String endpoints = "http://" + IP + ":2379,http://" + IP + ":2380,http://" + IP + ":2381";


    @Bean
    public EtcdService getEtcdService(){
        return new EtcdServiceImpl(endpoints);
    }

    @Bean
    public AdvancedEtcdService getAdvancedEtcdService(){
        return new AdvancedEtcdServiceImpl(endpoints);
    }

    public static void main(String[] args) {
        SpringApplication.run(AdvancedOperateApplication.class, args);
    }
}