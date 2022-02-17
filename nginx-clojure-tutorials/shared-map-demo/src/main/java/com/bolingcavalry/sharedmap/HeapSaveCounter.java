package com.bolingcavalry.sharedmap;

import nginx.clojure.java.ArrayMap;
import nginx.clojure.java.NginxJavaRingHandler;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static nginx.clojure.MiniConstants.CONTENT_TYPE;
import static nginx.clojure.MiniConstants.NGX_HTTP_OK;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/2/17 10:58 下午
 * @description 在堆内存中存储访问记录的handler
 */
public class HeapSaveCounter implements NginxJavaRingHandler {

    /**
     * 通过UUID来表明当前jvm进程的身份
     */
    private String tag = UUID.randomUUID().toString();

    private int requestCount = 1;

    @Override
    public Object[] invoke(Map<String, Object> map) throws IOException {

        String body = "From "
                    + tag
                    + ", total request count [ "
                    + requestCount++
                    + "]";

        return new Object[] {
                NGX_HTTP_OK, //http status 200
                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
                body
        };
    }
}