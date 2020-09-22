package com.bolingcavalry.webservice;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AvailabilityFilteringRule;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.PingUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * @Description: ribbon配置类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/6/16 11:52
 */
public class RibbonConfiguration {

    @Autowired
    IClientConfig ribbonClientConfig;

    /**
     * 检查服务是否可用的实例，
     * 此地址返回的响应的返回码如果是200表示服务可用
     * @param config
     * @return
     */
    @Bean
    public IPing ribbonPing(IClientConfig config){
        return new PingUrl();
    }

    /**
     * 轮询规则
     * @param config
     * @return
     */
    @Bean
    public IRule ribbonRule(IClientConfig config){
        return new AvailabilityFilteringRule();
    }
}
