package com.bolingcavalry.customizepropertyverify.context;

import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

/**
 * @Description : AnnotationConfigServletWebServerApplicationContext，重写了initPropertySources方法，
 * 要求spring启动的时候环境变量MYSQL_HOST必须存在
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-10 21:40
 */
public class CustomApplicationContext extends AnnotationConfigServletWebServerApplicationContext {

    @Override
    protected void initPropertySources() {
        super.initPropertySources();
        //把"MYSQL_HOST"作为启动的时候必须验证的环境变量
        getEnvironment().setRequiredProperties("MYSQL_HOST");
    }
}
