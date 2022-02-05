package com.bolingcavalry.simplehello;

import nginx.clojure.java.ArrayMap;
import nginx.clojure.java.NginxJavaRingHandler;

import java.time.LocalDateTime;
import java.util.Map;

import static nginx.clojure.MiniConstants.CONTENT_TYPE;
import static nginx.clojure.MiniConstants.NGX_HTTP_OK;

/**
 * @author zq2599@gmail.com
 * @Title: 产生内容的handler
 * @Package
 * @Description:
 * @date 2/1/22 12:41 PM
 */
public class HelloHandler implements NginxJavaRingHandler {

    @Override
    public Object[] invoke(Map<String, Object> request) {
        return new Object[] {
                NGX_HTTP_OK, //http status 200
                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
                "Hello, Nginx clojure! " + LocalDateTime.now()  //response body can be string, File or Array/Collection of them
        };
    }
}