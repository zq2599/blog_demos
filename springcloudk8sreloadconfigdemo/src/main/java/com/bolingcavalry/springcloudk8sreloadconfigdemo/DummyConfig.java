package com.bolingcavalry.springcloudk8sreloadconfigdemo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 配置类，此处可以加载配置文件中的内容
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/7/27 18:24
 */
@Configuration
@ConfigurationProperties(prefix = "greeting")
public class DummyConfig {

    private String message = "This is a dummy message";

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}