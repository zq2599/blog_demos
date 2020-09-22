package com.bolingcavalry.springbootproperties.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

@ApiModel(description = "JsonProperty注解测试类")
public class Test {

    @ApiModelProperty(value = "私有成员变量")
    @JsonProperty(value = "json_field0", index = 1)
    private Date field0 = new Date();

    public void setField0(Date field0) {
        this.field0 = field0;
    }

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