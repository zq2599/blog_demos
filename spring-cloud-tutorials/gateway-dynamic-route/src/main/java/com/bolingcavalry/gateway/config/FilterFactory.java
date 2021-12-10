package com.bolingcavalry.gateway.config;

import com.bolingcavalry.gateway.filter.BizLogicRouteGatewayFilterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterFactory {

    @Bean
    public BizLogicRouteGatewayFilterFactory buildBizLogicRouteFilter() {
        return new BizLogicRouteGatewayFilterFactory();
    }
}
