package com.bolingcavalry.springbootproperties.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: log相关的web接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:31
 */
@RestController
@RequestMapping("/jsonpropertyserial")
@Api(tags = {"JsonPropertySerializationController"})
public class JsonPropertySerializationController {

    @ApiModel(description = "JsonProperty注解测试类")
    static class Test {
        @ApiModelProperty(value = "私有成员变量")
        @JsonProperty(value = "json_field0", index = 1)
        private String field0;

        @ApiModelProperty(value = "来自get方法")
        @JsonProperty(value = "json_field1", index = 0)
        public String getField1() {
            return "111";
        }

        public Test(String field0) {
            this.field0 = field0;
        }
    }

    @ApiOperation(value = "测试JsonProperty注解", notes = "测试JsonProperty注解")
    @RequestMapping(value = "/serialization", method = RequestMethod.GET)
    public Test jsonproperty() {
        return new Test("000");
    }


}
