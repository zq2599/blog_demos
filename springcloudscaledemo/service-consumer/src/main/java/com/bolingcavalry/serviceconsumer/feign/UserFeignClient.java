package com.bolingcavalry.serviceconsumer.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description : feign接口
 * @Author : zq2599@gmail.com
 * @Date : 2018-01-25 13:57
 */
@FeignClient(name = "microservice-provider-user")
public interface UserFeignClient {

    @RequestMapping(value = "/getuserinfo", method = RequestMethod.GET)
    String getUserInfoWithRequestParam(@RequestParam("id") String id, @RequestParam("name") String name);
}
