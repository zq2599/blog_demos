package com.bolingcavalry.curd.controller;

import com.bolingcavalry.curd.entity.Log;
import com.bolingcavalry.curd.entity.LogExtend;
import com.bolingcavalry.curd.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: log相关的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
@RequestMapping("/log")
@Api(tags = {"LogController"})
public class LogController {
    @Autowired
    private LogService logService;

    @ApiOperation(value = "根据ID查找日志记录", notes="根据ID查找日志记录")
    @ApiImplicitParam(name = "id", value = "日志ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public LogExtend logExtend(@PathVariable int id){
        return logService.selExtend(id);
    }

    @ApiOperation(value = "新增日志记录", notes="新增日志记录")
    @RequestMapping(value = "/insertwithfields",method = RequestMethod.PUT)
    public Log create(@RequestBody Log log) {
        return logService.insertWithFields(log);
    }

}
