package com.bolingcavalry.probedemo.controller;

import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * description: StateReader <br>
 * date: 2020/6/4 下午4:38 <br>
 * author: willzhao <br>
 * email: zq2599@gmail.com <br>
 * version: 1.0 <br>
 */
@RestController
@RequestMapping("/statereader")
public class StateReader {

    @Resource
    ApplicationAvailability applicationAvailability;

    @RequestMapping(value="/get")
    public String state() {
        return "livenessState : " + applicationAvailability.getLivenessState()
               + "<br>readinessState : " + applicationAvailability.getReadinessState()
               + "<br>" + new Date();
    }
}
