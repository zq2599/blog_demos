package com.bolingcavalry.handlerdemo;

import nginx.clojure.Configurable;
import nginx.clojure.java.ArrayMap;
import nginx.clojure.java.NginxJavaRingHandler;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import static nginx.clojure.MiniConstants.CONTENT_TYPE;
import static nginx.clojure.MiniConstants.NGX_HTTP_OK;

/**
 * @author zq2599@gmail.com
 * @Title:
 * @Package
 * @Description:
 * @date 2/6/22 3:12 PM
 */
public class MyContentHandler implements NginxJavaRingHandler, Configurable {

    private Map<String, String> config;

    /**
     * location中配置的content_handler_property属性会通过此方法传给当前类
     * @param map
     */
    @Override
    public void config(Map<String, String> map) {
        this.config = map;
    }

    @Override
    public Object[] invoke(Map<String, Object> map) throws IOException {

        String body = "From MyContentHandler, "
                    + LocalDateTime.now()
                    + ", foo : "
                    + config.get("foo.name")
                    + ", bar : "
                    + config.get("bar.name");

        return new Object[] {
                NGX_HTTP_OK, //http status 200
                ArrayMap.create(CONTENT_TYPE, "text/plain"), //headers map
                body
        };
    }
}