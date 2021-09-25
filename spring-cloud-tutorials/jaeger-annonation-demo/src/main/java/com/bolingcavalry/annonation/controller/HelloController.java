package com.bolingcavalry.annonation.controller;

import com.bolingcavalry.annonation.aop.MyChildSpan;
import com.bolingcavalry.annonation.aop.MySpan;
import com.bolingcavalry.annonation.service.Biz;
import com.bolingcavalry.common.Constants;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Slf4j
public class HelloController {

    Biz biz;

    public HelloController(Biz biz) {
        this.biz = biz;
    }

    /**
     * 返回字符串类型
     * @return
     */
    @GetMapping("/hello")
    public String hello() {
        // 调用service层的服务
        biz.mock();

        // 返回
        return new Date().toString();
    }

}
