package com.bolingcavalry.relatedoperation.controller;

import com.bolingcavalry.relatedoperation.entity.UserWithLogs;
import com.bolingcavalry.relatedoperation.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation(value = "根据ID查找user记录（包含行为日志），通过left join实现", notes="根据ID查找user记录（包含行为日志），通过left join实现")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/leftjoin/{id}", method = RequestMethod.GET)
    public UserWithLogs selUserWithLogsLeftJoin(@PathVariable int id){
        return userService.selUserWithLogsLeftJoin(id);
    }

    @ApiOperation(value = "根据ID查找user记录（包含行为日志），通过嵌套查询实现", notes="根据ID查找user记录（包含行为日志），通过嵌套查询实现")
    @ApiImplicitParam(name = "id", value = "用户ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/nested/{id}", method = RequestMethod.GET)
    public UserWithLogs selUserWithLogsNestedSelect(@PathVariable int id){
        return userService.selUserWithLogsNestedSelect(id);
    }

}
