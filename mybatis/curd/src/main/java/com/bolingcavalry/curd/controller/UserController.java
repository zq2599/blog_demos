package com.bolingcavalry.curd.controller;

import com.bolingcavalry.curd.entity.User;
import com.bolingcavalry.curd.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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

    @RequestMapping(value = "user/insertwithfields/{name}/{age}", method = RequestMethod.GET)
    public User insertWithFields(@PathVariable("name") String name, @PathVariable("age")  int age) {
        User user = new User();
        user.setName(name);
        user.setAge(age);

        return userService.insertWithFields(user);
    }

    @RequestMapping(value = "user/insertbatch/{namePrefix}/{age}", method = RequestMethod.GET)
    public List<User> insertBatch(@PathVariable("namePrefix") String namePrefix, @PathVariable("age")  int age) {
        List<User> list = new ArrayList<>();

        for(int i=0;i<10;i++) {
            User user = new User();
            user.setName(namePrefix + "-" + i);
            user.setAge(age + i);

            list.add(user);
        }

        return userService.insertBatch(list);
    }

}
