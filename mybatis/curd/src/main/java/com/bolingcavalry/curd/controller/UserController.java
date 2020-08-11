package com.bolingcavalry.curd.controller;

import com.bolingcavalry.curd.entity.User;
import com.bolingcavalry.curd.service.UserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: user表操作的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;


    @ApiOperation(value = "create user", notes="新增user记录")
    @RequestMapping(value = "/insertwithfields",method = RequestMethod.PUT)
    public User create(@RequestBody User user) {
        return userService.insertWithFields(user);
    }

    @ApiOperation(value = "batch create user", notes="批量新增user记录")
    @RequestMapping(value = "/insertbatch", method = RequestMethod.PUT)
    public List<User> insertBatch(@RequestBody List<User> users) {
        return userService.insertBatch(users);
    }

    @ApiOperation(value = "delete user", notes="删除指定ID的user记录")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public int delete(@PathVariable int id){
        return userService.delete(id);
    }


    @ApiOperation(value = "clear all user", notes="删除user表所有数据")
    @RequestMapping(value = "/clearall", method = RequestMethod.DELETE)
    public int clearAll(){
        return userService.clearAll();
    }

    @ApiOperation(value = "find by name", notes="根据名称模糊查找所有user记录")
    @ApiImplicitParam(name = "name", value = "用户名", paramType = "path", required = true, dataType = "String")
    @RequestMapping(value = "/findbyname/{name}", method = RequestMethod.GET)
    public List<User> findByName(@PathVariable("name") String name){
        return userService.findByName(name);
    }

    @ApiOperation(value = "update by id", notes="根据ID修改user记录")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public int update(@RequestBody User user){
        return userService.update(user);
    }

    @ApiOperation(value = "find by id", notes="根据ID查找user记录")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String GetUser(@PathVariable int id){
        return userService.sel(id).toString();
    }
}
