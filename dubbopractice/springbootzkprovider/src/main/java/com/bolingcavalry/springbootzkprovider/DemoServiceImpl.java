package com.bolingcavalry.springbootzkprovider;

import com.bolingcavalry.dubbopractice.service.DemoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;

/**
 * @Description: 服务提供方
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/17 11:35
 */
@Slf4j
@Service
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        log.info("I'm springboot-zk-provider, Hello " + name + ", request from consumer: " + RpcContext.getContext().getRemoteAddress());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "I'm springboot-zk-provider, Hello " + name + ", response from provider: " + RpcContext.getContext().getLocalAddress();
    }
}
