package com.bolingcavalry.springbootproperties.controller;

import com.bolingcavalry.springbootproperties.bean.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 序列化和反序列化测试
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
@RequestMapping("/jsonproperty")
@Api(tags = {"JsonPropertySerializationController"})
public class JsonPropertySerializationController {

    private static final Logger logger = LoggerFactory.getLogger(JsonPropertySerializationController.class);

    @Autowired
    ObjectMapper mapper;

    @ApiOperation(value = "测试序列化", notes = "测试序列化")
    @RequestMapping(value = "/serialization", method = RequestMethod.GET)
    public Test serialization() throws JsonProcessingException {

        Test test = new Test();
        logger.info(mapper.writeValueAsString(test));

        return test;
    }

    @ApiOperation(value = "测试反序列化", notes="测试反序列化")
    @RequestMapping(value = "/deserialization",method = RequestMethod.PUT)
    public String deserialization(@RequestBody Test test) {
        return test.toString();
    }
}
