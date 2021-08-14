package com.bolingcavalry.helloxmldirectprovider.service.impl;

import com.bolingcavalry.dubbopractice.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcContext;

/**
 * @Description: 服务提供方
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/17 11:35
 */
@Slf4j
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        log.info("Hello " + name + ", request from consumer: " + RpcContext.getContext().getRemoteAddress());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello " + name + ", response from provider: " + RpcContext.getContext().getLocalAddress();
    }
}
