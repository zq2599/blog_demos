package com.bolingcavalry.changebody.config;

import com.bolingcavalry.changebody.function.RequestBodyRewrite;
import com.bolingcavalry.changebody.function.ResponseBodyRewrite;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/9/4 8:44
 */
@Configuration
public class FilterConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder, ObjectMapper objectMapper) {
        return builder
                .routes()
                .route("path_route_change",
                        r -> r.path("/hello/change")
                                .filters(f -> f
                                        .modifyRequestBody(String.class,String.class,new RequestBodyRewrite(objectMapper))
                                        .modifyResponseBody(String.class, String.class, new ResponseBodyRewrite(objectMapper))
                                        )
                        .uri("http://127.0.0.1:8082"))
                .build();
    }

}
