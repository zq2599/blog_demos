package com.bolingcavalry.interceptor.define;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/5/1 23:32
 * @description 发送通知的拦截器定义，可以通过设置value值来指定消息类型是短信或邮件
 */
@InterceptorBinding
@Repeatable(SendMessage.SendMessageList.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SendMessage {

    /**
     * 消息类型 : "sms"表示短信，"email"表示邮件
     * @return
     */
    @Nonbinding
    String sendType() default "sms";

    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface SendMessageList {
        SendMessage[] value();
    }
}
