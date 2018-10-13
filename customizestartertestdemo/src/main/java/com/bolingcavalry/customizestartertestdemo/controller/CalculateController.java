package com.bolingcavalry.customizestartertestdemo.controller;

import com.bolingcavalry.api.exception.MinusException;
import com.bolingcavalry.api.service.AddService;
import com.bolingcavalry.api.service.MinusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author wilzhao
 * @description 调用加法和减法服务的测试类
 * @email zq2599@gmail.com
 * @time 2018/10/13 16:00
 */
@RestController
public class CalculateController {

    @Autowired
    private AddService addService;

    @Autowired
    private MinusService minusService;

    @RequestMapping(value = "/add/{added}/{add}", method = RequestMethod.GET)
    public String add(@PathVariable("added") int added, @PathVariable("add") int add){
        return added + " 加 " + add + " 等于 : " + addService.add(added, add);
    }

    @RequestMapping(value = "/minus/{minuend}/{subtraction}", method = RequestMethod.GET)
    public String minus(@PathVariable("minuend") int minuend, @PathVariable("subtraction") int subtraction) throws MinusException {
        return minuend + " 减 " + subtraction + " 等于 : " + minusService.minus(minuend, subtraction);
    }
}
