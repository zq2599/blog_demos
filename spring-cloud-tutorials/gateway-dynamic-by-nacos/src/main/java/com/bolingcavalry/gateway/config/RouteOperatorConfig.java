package com.bolingcavalry.gateway.config;

import com.bolingcavalry.gateway.service.RouteOperator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author willzhao
 * @version 1.0
 * @description 监听配置
 * @date 2021/8/14 17:21
 */
@Configuration
public class RouteOperatorConfig {
    @Bean
    public RouteOperator routeOperator(ObjectMapper objectMapper,
                                       RouteDefinitionWriter routeDefinitionWriter,
                                       ApplicationEventPublisher applicationEventPublisher) {

        return new RouteOperator(objectMapper,
                routeDefinitionWriter,
                applicationEventPublisher);
    }
}
