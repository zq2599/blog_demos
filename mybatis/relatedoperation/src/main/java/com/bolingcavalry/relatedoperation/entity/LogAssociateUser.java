package com.bolingcavalry.relatedoperation.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * @Description: 实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:24
 */
@Data
@NoArgsConstructor
@ApiModel(description = "日志实体类")
public class LogAssociateUser {
    @ApiModelProperty(value = "日志ID")
    private Integer id;

    @ApiModelProperty(value = "用户对象")
    private User user;

    @ApiModelProperty(value = "日志内容")
    private String action;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
