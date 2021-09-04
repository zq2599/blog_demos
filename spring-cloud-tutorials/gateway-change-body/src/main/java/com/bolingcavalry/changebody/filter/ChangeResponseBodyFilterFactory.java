package com.bolingcavalry.changebody.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/28 9:33
 */
@Component
public class ChangeResponseBodyFilterFactory extends AbstractGatewayFilterFactory<Object>
{
    @Autowired
    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String name() {
        return "ChangeResponseBody";
    }

    @Override
    public GatewayFilter apply(Object config)
    {
        return new ChangeResponseBodyFilter(modifyResponseBodyFilter, objectMapper);

//        return (exchange, chain) -> {
//            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//                ServerHttpResponse response = exchange.getResponse();
//                //Manipulate the response in some way
//
//            }));
//        };


    }
}