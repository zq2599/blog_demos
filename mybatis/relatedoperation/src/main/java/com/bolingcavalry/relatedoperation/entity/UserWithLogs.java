package com.bolingcavalry.relatedoperation.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: 用户实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:24
 */
@Data
@NoArgsConstructor
@ApiModel(description = "用户实体类（含行为日志集合）")
public class UserWithLogs {

    @ApiModelProperty(value = "用户ID")
    private Integer id;

    @ApiModelProperty(value = "用户名", required = true)
    private String name;

    @ApiModelProperty(value = "用户地址", required = false)
    private Integer age;

    @ApiModelProperty(value = "行为日志", required = false)
    private List<Log> logs;
}
