package com.bolingcavalry.jaeger.provider.controller;

import com.bolingcavalry.common.Constants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/9/12 7:26 上午
 * @description 功能介绍
 */
@RestController
public class HelloController {

    private String dateStr(){
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }

    /**
     * 返回字符串类型
     * @return
     */
    @GetMapping("/hello")
    public String hello() {
        return Constants.HELLO_PREFIX + ", " + dateStr();
    }

}
