package com.bolingcavalry.sharedmap;

import nginx.clojure.java.ArrayMap;
import nginx.clojure.java.NginxJavaRingHandler;
import nginx.clojure.util.NginxSharedHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static nginx.clojure.MiniConstants.CONTENT_TYPE;
import static nginx.clojure.MiniConstants.NGX_HTTP_OK;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/2/17 10:58 下午
 * @description 在共享内存中存储访问记录的handler
 */
public class SharedMapSaveCounter implements NginxJavaRingHandler {

    /**
     * 通过UUID来表明当前jvm进程的身份
     */
    private String tag = UUID.randomUUID().toString();

    private NginxSharedHashMap smap = NginxSharedHashMap.build("uri_access_counters");

    @Override
    public Object[] invoke(Map<String, Object> map) throws IOException {
        String uri = (String)map.get("uri");

        // 尝试在共享内存中新建key，并将其值初始化为1，
        // 如果初始化成功，返回值就是0，
        // 如果返回值不是0，表示共享内存中该key已经存在
        int rlt = smap.putIntIfAbsent(uri, 1);

        // 如果rlt不等于0，表示这个key在调用putIntIfAbsent之前已经在共享内存中存在了，
        // 此时要做的就是加一，
        // 如果relt等于0，就把rlt改成1，表示访问总数已经等于1了
        if (0==rlt) {
            rlt++;
        } else {
            // 原子性加一，这样并发的时候也会顺序执行
            rlt = smap.atomicAddInt(uri, 1);
            rlt++;
        }

        // 返回的body内容，要体现出JVM的身份，以及share map中的计数
        String body = "From "
                + tag
                + ", total request count [ "
                + rlt
                + "]";

        return new Object[] {
                NGX_HTTP_OK, //http status 200
                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
                body
        };
    }
}