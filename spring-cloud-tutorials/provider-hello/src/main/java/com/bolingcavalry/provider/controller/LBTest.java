package com.bolingcavalry.provider.controller;

import com.bolingcavalry.common.Constants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zq2599@gmail.com
 * @Title: web服务
 * @Package
 * @Description:
 * @date 8/8/21 4:32 下午
 */
@RestController
@RequestMapping("/lbtest")
public class LBTest {

    private String dateStr(){
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }

    /**
     * 返回字符串类型
     * @return
     */
    @GetMapping("/str")
    public String helloStr() {
        return Constants.LB_PREFIX + ", " + dateStr();
    }
}