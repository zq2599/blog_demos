package com.bolingcavalry.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 测试用的controller，会远程调用account-service的服务
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/6/16 11:46
 */
@RestController
public class WebServiceController {

    @Autowired
    private AccountService accountService;

    /**
     * 探针检查响应类
     * @return
     */
    @RequestMapping("/health")
    public String health() {
        return "OK";
    }

    /**
     * 远程调用account-service提供的服务
     * @return 多次远程调返回的所有结果.
     */
    @RequestMapping("/account")
    public String account() {

        StringBuilder sbud = new StringBuilder();

        for(int i=0;i<10;i++){
            sbud.append(accountService.getDataFromSpringCloudK8SProvider())
                .append("<br>");
        }

        return sbud.toString();
    }
}
