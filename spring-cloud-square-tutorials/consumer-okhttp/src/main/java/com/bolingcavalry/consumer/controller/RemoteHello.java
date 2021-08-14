package com.bolingcavalry.consumer.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author zq2599@gmail.com
 * @Title: 服务消费者
 * @Package
 * @Description:
 * @date 7/31/21 7:54 上午
 */
@RestController
public class RemoteHello {
    @Autowired
    private OkHttpClient.Builder builder;

    @GetMapping("/remote-str")
    public String hello() throws IOException {
        // 直接使用服务名
        Request request = new Request.Builder().url("http://provider/hello-str").build();

        // 远程访问
        Response response = builder.build().newCall(request).execute();

        return "get remote response : " + response.body().string();
    }
}