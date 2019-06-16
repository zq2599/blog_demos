package com.bolingcavalry.webservice;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 这里面封装了远程调用account-service提供服务的逻辑
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/6/16 12:21
 */
@Service
public class AccountService {

    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "getFallbackName" ,commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000") })
    public String getDataFromSpringCloudK8SProvider(){
        return this.restTemplate.getForObject("http://account-service/name", String.class);
    }

    /**
     * 熔断时调用的方法
     * @return
     */
    private String getFallbackName() {
        return "Fallback"
                + ", "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
