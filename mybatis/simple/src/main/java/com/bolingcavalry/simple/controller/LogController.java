package com.bolingcavalry.simple.controller;

import com.bolingcavalry.simple.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: log相关的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
public class LogController {
    @Autowired
    private LogService logService;

    @RequestMapping("log/{id}")
    public String log(@PathVariable int id){
        return logService.sel(id).toString();
    }

}
