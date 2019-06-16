package com.bolingcavalry.springcloudk8sconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 测试用的controller，会远程调用springcloudk8sprovider的服务
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/6/16 11:46
 */
@RestController
public class ConsumerController {

    @Autowired
    private ProviderService providerService;

    /**
     * 探针检查响应类
     * @return
     */
    @RequestMapping("/health")
    public String health() {
        return "OK";
    }

    /**
     * 远程调用springcloudk8sprovider提供的服务
     * @return 当前应用所在容器的hostname.
     */
    @RequestMapping("/consume")
    public String consume(
            @RequestParam(value = "delay", defaultValue = "0") int delayValue) {

        StringBuilder sbud = new StringBuilder();


        for(int i=0;i<10;i++){
            sbud.append(providerService.getDataFromSpringCloudK8SProvider())
                .append("<br>");
        }

        return sbud.toString();
    }
}
