package com.bolingcavalry.service.impl;

import io.quarkus.logging.Log;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class RequestScopeBean {

    /**
     * 在构造方法中打印日志，通过日志出现次数对应着实例化次数
     */
    public RequestScopeBean() {
        Log.info("Instance of " + this.getClass().getSimpleName());
    }

    public String hello() {
        return "from " + this.getClass().getSimpleName();
    }
}
