package com.bolingcavalry.springbootzkconsumer.controller;

import com.bolingcavalry.springbootzkconsumer.service.RemoteInvokeServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/17 15:36
 */
@RestController
@RequestMapping("/demo")
@Api(tags = {"DemoController"})
public class DemoController {
    @Autowired
    private RemoteInvokeServiceImpl remoteInvokeService;

    @ApiOperation(value = "获取dubbo service provider的响应", notes="\"获取dubbo service provider的响应")
    @ApiImplicitParam(name = "name", value = "昵称", paramType = "path", required = true, dataType = "String")
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public String sayHello(@PathVariable String name){
        return remoteInvokeService.sayHello(name);
    }

}
