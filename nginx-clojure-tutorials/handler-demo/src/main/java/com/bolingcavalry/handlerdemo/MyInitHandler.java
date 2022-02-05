package com.bolingcavalry.handlerdemo;

import nginx.clojure.NginxClojureRT;
import nginx.clojure.java.NginxJavaRingHandler;
import java.io.IOException;
import java.util.Map;

/**
 * @author zq2599@gmail.com
 * @Title: 初始化handler
 * @Package
 * @Description:
 * @date 2/5/22 10:48 PM
 */
public class MyInitHandler implements NginxJavaRingHandler {
    @Override
    public Object[] invoke(Map<String, Object> map) throws IOException {
        // 可以根据实际需求执行初始化操作，这里作为演示，只打印日志
        NginxClojureRT.log.info("MyInitHandler.invoke executed");
        // 返回null表示初始化成功，如果初始化失败，不想让这个worker正常运行，请返回： new Object[] {500, null, null};
        return null;
    }
}