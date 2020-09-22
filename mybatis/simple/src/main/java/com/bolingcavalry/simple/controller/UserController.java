package com.bolingcavalry.simple.controller;

import com.bolingcavalry.simple.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: user表操作的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("user/{id}")
    public String GetUser(@PathVariable int id){
        return userService.sel(id).toString();
    }

}
