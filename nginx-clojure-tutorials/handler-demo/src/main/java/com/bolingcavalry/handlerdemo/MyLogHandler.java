package com.bolingcavalry.handlerdemo;

import nginx.clojure.Configurable;
import nginx.clojure.NginxClojureRT;
import nginx.clojure.java.NginxJavaRequest;
import nginx.clojure.java.NginxJavaRingHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author zq2599@gmail.com
 * @Title:
 * @Package
 * @Description:
 * @date 2/7/22 11:59 PM
 */
public class MyLogHandler implements NginxJavaRingHandler, Configurable {

    /**
     * 是否将User Agent打印在日志中
     */
    private boolean logUserAgent;

    /**
     * 日志文件路径
     */
    private String filePath;

    @Override
    public Object[] invoke(Map<String, Object> request) throws IOException {
        File file = new File(filePath);
        NginxJavaRequest r = (NginxJavaRequest) request;
        try (FileOutputStream out = new FileOutputStream(file, true)) {
            String msg = String.format("%s - %s [%s] \"%s\" %s \"%s\" %s %s\n",
                    r.getVariable("remote_addr"),
                    r.getVariable("remote_user", "x"),
                    r.getVariable("time_local"),
                    r.getVariable("request"),
                    r.getVariable("status"),
                    r.getVariable("body_bytes_sent"),
                    r.getVariable("http_referer", "x"),
                    logUserAgent ? r.getVariable("http_user_agent") : "-");
            out.write(msg.getBytes("utf8"));
        }
        return null;
    }

    @Override
    public void config(Map<String, String> properties) {
        logUserAgent = "on".equalsIgnoreCase(properties.get("log.user.agent"));
        filePath = properties.get("log.file.path");
        NginxClojureRT.log.info("MyLogHandler, logUserAgent [" + logUserAgent + "], filePath [" + filePath + "]");
    }

    // 下面这段代码来自官方demo，实测发现这段代码在打印日志的逻辑中并未发挥作用，
    // 不论是否删除，日志输出的内容都是相同的
    /*
    @Override
    public String[] variablesNeedPrefetch() {
        return new String[] { "remote_addr", "remote_user", "time_local", "request", "status", "body_bytes_sent",
                "http_referer", "http_user_agent" };
    }
    */
}