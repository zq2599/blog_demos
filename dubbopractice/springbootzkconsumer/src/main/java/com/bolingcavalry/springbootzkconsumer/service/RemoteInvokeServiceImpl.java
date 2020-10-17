package com.bolingcavalry.springbootzkconsumer.service;

import com.bolingcavalry.dubbopractice.service.DemoService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/10/17 15:33
 */
@Service
public class RemoteInvokeServiceImpl {

    @Reference
    private DemoService demoService;

    public String sayHello(String name) {
        return "from dubbo remote (zk registry center mode) : " + demoService.sayHello(name);
    }
}
