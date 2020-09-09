package com.bolingcavalry.springbootproperties.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @Description: log相关的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
@RequestMapping("/jsonproperty")
@Api(tags = {"JsonPropertySerializationController"})
public class JsonPropertySerializationController {

    @ApiModel(description = "JsonProperty注解测试类")
    static class Test {

        @ApiModelProperty(value = "私有成员变量")
        @JsonProperty(value = "json_field0", index = 1)
        private Date field0 = new Date();

        @ApiModelProperty(value = "来自get方法的字符串")
        @JsonProperty(value = "json_field1", index = 0)
        public String getField1() {
            return "111";
        }

        @Override
        public String toString() {
            return "Test{" +
                    "field0=" + field0 +
                    '}';
        }

    }

    @ApiOperation(value = "测试序列化", notes = "测试序列化")
    @RequestMapping(value = "/serialization", method = RequestMethod.GET)
    public Test jsonproperty() {
        return new Test();
    }


    @ApiOperation(value = "测试反序列化", notes="测试反序列化")
    @RequestMapping(value = "/deserialization",method = RequestMethod.PUT)
    public String create(@RequestBody Test test) {
        return test.toString();
    }

}
