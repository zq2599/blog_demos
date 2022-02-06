package com.bolingcavalry.handlerdemo;

import nginx.clojure.NginxClojureRT;
import nginx.clojure.java.NginxJavaRingHandler;
import java.io.IOException;
import java.util.Map;

public class MyInitHandler implements NginxJavaRingHandler {
    @Override
    public Object[] invoke(Map<String, Object> map) throws IOException {
        NginxClojureRT.log.info("1. MyInitHandler.invoke executed");
//        return new Object[] {500, null, null};
        return null;
    }
}