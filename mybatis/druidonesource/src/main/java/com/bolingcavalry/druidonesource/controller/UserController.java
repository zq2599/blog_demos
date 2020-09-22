package com.bolingcavalry.druidonesource.controller;

import com.bolingcavalry.druidonesource.entity.User;
import com.bolingcavalry.druidonesource.service.UserService;
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

    @ApiOperation(value = "批量新增user记录", notes="批量新增user记录")
    @RequestMapping(value = "/insertbatch", method = RequestMethod.PUT)
    public List<User> insertBatch(@RequestBody List<User> users) {
        return userService.insertBatch(users);
    }

    @ApiOperation(value = "删除指定ID的user记录", notes="删除指定ID的user记录")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public int delete(@PathVariable int id){
        return userService.delete(id);
    }

    @ApiOperation(value = "删除user表所有数据", notes="删除user表所有数据")
    @RequestMapping(value = "/clearall", method = RequestMethod.DELETE)
    public int clearAll(){
        return userService.clearAll();
    }

    @ApiOperation(value = "根据ID修改user记录", notes="根据ID修改user记录")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public int update(@RequestBody User user){
        return userService.update(user);
    }

    @ApiOperation(value = "根据名称模糊查找所有user记录", notes="根据名称模糊查找所有user记录")
    @ApiImplicitParam(name = "name", value = "用户名", paramType = "path", required = true, dataType = "String")
    @RequestMapping(value = "/findbyname/{name}", method = RequestMethod.GET)
    public List<User> findByName(@PathVariable("name") String name){
        return userService.findByName(name);
    }

    @ApiOperation(value = "根据ID查找user记录", notes="根据ID查找user记录")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public User GetUser(@PathVariable int id){
        return userService.sel(id);
    }

    @ApiOperation(value = "获取总数", notes="获取总数")
    @RequestMapping(value = "/totalcount", method = RequestMethod.GET)
    public int totalcount(){
        return userService.totalCount();
    }
}
