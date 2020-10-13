package com.bolingcavalry.relatedoperation.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:24
 */
@Data
@NoArgsConstructor
@ApiModel(description = "日志实体类(含用户表的字段)")
public class LogExtend extends Log {

    @ApiModelProperty(value = "用户名")
    private String userName;
}
