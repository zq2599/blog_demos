package com.bolingcavalry.changebody.filter;


import com.bolingcavalry.changebody.function.RequestBodyRewrite;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/28 9:32
 */
public class ChangeRequestBodyFilter implements GatewayFilter {

    public ChangeRequestBodyFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter, ObjectMapper objectMapper) {
        this.modifyRequestBodyFilter = modifyRequestBodyFilter;
        this.objectMapper = objectMapper;
    }

    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return modifyRequestBodyFilter
                .apply(new ModifyRequestBodyGatewayFilterFactory
                            .Config()
                            .setRewriteFunction(String.class, String.class, new RequestBodyRewrite(objectMapper)))
                .filter(exchange, chain);

    }
}