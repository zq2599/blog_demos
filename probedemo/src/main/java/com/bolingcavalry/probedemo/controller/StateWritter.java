package com.bolingcavalry.probedemo.controller;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * description: 修改状态的controller <br>
 * date: 2020/6/4 下午1:21 <br>
 * author: willzhao <br>
 * email: zq2599@gmail.com <br>
 * version: 1.0 <br>
 */
@RestController(value = "/statewritter")
public class StateWritter {

    @Resource
    ApplicationEventPublisher applicationEventPublisher;

    /**
     * 将存活状态改为broken（会导致kubernetes杀死pod）
     * @return
     */
    @RequestMapping(value="/broken")
    public String broken(){
        AvailabilityChangeEvent.publish(applicationEventPublisher, StateWritter.this, LivenessState.BROKEN);
        return "success" + new Date();
    }

}
