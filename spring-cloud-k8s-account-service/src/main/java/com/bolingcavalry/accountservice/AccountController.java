package com.bolingcavalry.accountservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 提供服务的controller
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/6/16 10:22
 */
@RestController
public class AccountController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

    private final String hostName = System.getenv("HOSTNAME");

    /**
     * 探针检查响应类
     * @return
     */
    @RequestMapping("/health")
    public String health() {
        return "OK";
    }

    @RequestMapping("/")
    public String ribbonPing(){
        LOG.info("ribbonPing of {}", hostName);
        return hostName;
    }

    /**
     * 返回hostname
     * @return 当前应用所在容器的hostname.
     */
    @RequestMapping("/name")
    public String getName() {
        return this.hostName
                + ", "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
