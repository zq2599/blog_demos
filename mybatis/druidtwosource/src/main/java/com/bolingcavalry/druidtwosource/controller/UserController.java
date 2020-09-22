package com.bolingcavalry.druidtwosource.controller;

import com.bolingcavalry.druidtwosource.entity.User;
import com.bolingcavalry.druidtwosource.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description: user表操作的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
@RequestMapping("/user")
@Api(tags = {"UserController"})
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "新增user记录", notes="新增user记录")
    @RequestMapping(value = "/insertwithfields",method = RequestMethod.PUT)
    public User create(@RequestBody User user) {
        return userService.insertWithFields(user);
    }

    @ApiOperation(value = "删除指定ID的user记录", notes="删除指定ID的user记录")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public int delete(@PathVariable int id){
        return userService.delete(id);
    }

    @ApiOperation(value = "根据名称模糊查找所有user记录", notes="根据名称模糊查找所有user记录")
    @ApiImplicitParam(name = "name", value = "用户名", paramType = "path", required = true, dataType = "String")
    @RequestMapping(value = "/findbyname/{name}", method = RequestMethod.GET)
    public List<User> findByName(@PathVariable("name") String name){
        return userService.findByName(name);
    }
}
