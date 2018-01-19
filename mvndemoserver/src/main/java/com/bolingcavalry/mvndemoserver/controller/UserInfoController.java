package com.bolingcavalry.mvndemoserver.controller;

import com.bolingcavalry.bean.UserInfo;
import org.springframework.web.bind.annotation.*;

/**
 * @Description : 返回用户信息的controller
 * @Author : zq2599@gmail.com
 * @Date : 2018-01-19 14:56
 */
@RestController
public class UserInfoController {

    @RequestMapping(value = "/getuserinfo/{name}", method = RequestMethod.GET)
    @ResponseBody
    public UserInfo getuserinfo(@PathVariable("name") final String name) {
       UserInfo userInfo = new UserInfo();
       userInfo.setName(name);
       userInfo.setAge(name.length());
       return userInfo;
    }
}
