package com.bolingcavalry.filterdemo;

import nginx.clojure.java.Constants;
import nginx.clojure.java.NginxJavaHeaderFilter;
import java.util.Map;

/**
 * @author zq2599@gmail.com
 * @Title: header filter的demo，删除已有header，增加新的header
 * @Package
 * @Description:
 * @date 2/8/22 4:28 PM
 */
public class RemoveAndAddMoreHeaders implements NginxJavaHeaderFilter {
    @Override
    public Object[] doFilter(int status, Map<String, Object> request, Map<String, Object> responseHeaders) {
        // 先删再加，相当于修改了Content-Type的值
        responseHeaders.remove("Content-Type");
        responseHeaders.put("Content-Type", "text/html");

        // 增加两个header
        responseHeaders.put("Xfeep-Header", "Hello2!");
        responseHeaders.put("Server", "My-Test-Server");

        // 返回PHASE_DONE表示告知nginx-clojure框架，当前filter正常，可以继续执行其他的filter和handler
        return Constants.PHASE_DONE;
    }
}