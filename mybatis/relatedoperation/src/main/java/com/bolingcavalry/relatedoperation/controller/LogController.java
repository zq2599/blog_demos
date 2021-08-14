package com.bolingcavalry.relatedoperation.controller;

import com.bolingcavalry.relatedoperation.entity.LogAssociateUser;
import com.bolingcavalry.relatedoperation.entity.LogExtend;
import com.bolingcavalry.relatedoperation.service.LogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation(value = "根据ID查找日志记录，带userName字段，该字段通过联表查询实现", notes="根据ID查找日志记录，带userName字段，该字段通过联表查询实现")
    @ApiImplicitParam(name = "id", value = "日志ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/aggregate/{id}", method = RequestMethod.GET)
    public LogExtend oneObjectSel(@PathVariable int id){
        return logService.oneObjectSel(id);
    }

    @ApiOperation(value = "根据ID查找日志记录，带用户对象，联表查询实现", notes="根据ID查找日志记录，带用户对象，联表查询实现")
    @ApiImplicitParam(name = "id", value = "日志ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/leftjoin/{id}", method = RequestMethod.GET)
    public LogAssociateUser leftJoinSel(@PathVariable int id){
        return logService.leftJoinSel(id);
    }

    @ApiOperation(value = "根据ID查找日志记录，带用户对象，嵌套查询实现", notes="根据ID查找日志记录，带用户对象，嵌套查询实现")
    @ApiImplicitParam(name = "id", value = "日志ID", paramType = "path", required = true, dataType = "Integer")
    @RequestMapping(value = "/nested/{id}", method = RequestMethod.GET)
    public LogAssociateUser nestedSel(@PathVariable int id){
        return logService.nestedSel(id);
    }
}
