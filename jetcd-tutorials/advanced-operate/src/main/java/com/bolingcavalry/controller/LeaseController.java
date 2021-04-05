package com.bolingcavalry.controller;

import com.bolingcavalry.dao.AdvancedEtcdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Description: 和租约相关的web服务
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/4/4 14:54
 */
@RestController
@Slf4j
public class LeaseController {

    @Autowired
    AdvancedEtcdService advancedEtcdService;

    @RequestMapping(value = "/lease/{key}/{value}", method = RequestMethod.GET)
    public String lease(@PathVariable("key") String key, @PathVariable("value") String value) throws Exception {
        advancedEtcdService.putWithLease(key, value);
        return "lease success " + new Date();
    }
}
