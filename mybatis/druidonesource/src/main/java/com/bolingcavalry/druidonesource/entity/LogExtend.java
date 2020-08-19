package com.bolingcavalry.druidonesource.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @Description: 实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/8/4 8:24
 */
@ApiModel(description = "日志实体类(含用户表的字段)")
public class LogExtend extends Log {


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @ApiModelProperty(value = "用户名")
    private String userName;

    @Override
    public String toString() {
        return "LogExtend{" +
                "id=" + getId() +
                ", userId=" + getUserId() +
                ", userName='" + getUserName() + '\'' +
                ", action='" + getAction() + '\'' +
                ", createTime=" + getCreateTime() +
                '}';
    }
}
