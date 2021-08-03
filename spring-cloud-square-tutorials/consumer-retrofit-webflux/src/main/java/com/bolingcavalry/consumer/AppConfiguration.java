package com.bolingcavalry.consumer;

import okhttp3.OkHttpClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.square.retrofit.webclient.EnableRetrofitClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author zq2599@gmail.com
 * @Title:
 * @Package
 * @Description:
 * @date 7/30/21 11:16 下午
 */
@Configuration
@EnableRetrofitClients
class AppConfiguration {
    @Bean
    @LoadBalanced
    public WebClient.Builder builder() {
        return WebClient.builder();
    }
}