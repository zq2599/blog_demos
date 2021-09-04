package com.bolingcavalry.provider.controller;

import com.bolingcavalry.common.Constants;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author zq2599@gmail.com
 * @Title: web服务
 * @Package
 * @Description:
 * @date 8/8/21 4:32 下午
 */
@RestController
@RequestMapping("/hello")
public class Hello {

    private String dateStr(){
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }

    /**
     * 返回字符串类型
     * @return
     */
    @GetMapping("/str")
    public String helloStr() {
        return Constants.HELLO_PREFIX + ", " + dateStr();
    }

    @RequestMapping(value = "/account/{id}", method = RequestMethod.GET)
    public String account(@PathVariable("id") int id) throws InterruptedException {
        if(1==id) {
            Thread.sleep(500);
        }

        return Constants.ACCOUNT_PREFIX + dateStr();
    }

    @GetMapping("/userinfo")
    public String userInfo(@RequestParam("username") String username) {
        return Constants.HELLO_PREFIX + " " + username + ", " + dateStr();
    }

    @PostMapping("/change")
    public Map<String, Object> change(@RequestBody Map<String, Object> map) {
        map.put("response-tag", dateStr());
        return map;
    }

}