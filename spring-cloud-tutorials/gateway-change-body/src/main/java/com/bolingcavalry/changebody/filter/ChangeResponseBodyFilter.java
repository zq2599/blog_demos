package com.bolingcavalry.changebody.filter;


import com.bolingcavalry.changebody.function.RequestBodyRewrite;
import com.bolingcavalry.changebody.function.ResponseBodyRewrite;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/28 9:32
 */
@Slf4j
public class ChangeResponseBodyFilter implements GatewayFilter {

    public ChangeResponseBodyFilter(ModifyResponseBodyGatewayFilterFactory modifyRequestBodyFilter, ObjectMapper objectMapper) {
        this.modifyResponseBodyFilter = modifyRequestBodyFilter;
        this.objectMapper = objectMapper;
    }

    private ModifyResponseBodyGatewayFilterFactory modifyResponseBodyFilter;

    private ObjectMapper objectMapper;

    private static final String REQUEST_TIME_BEGIN = "requestTimeBegin";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

//        return chain.filter(exchange).then(modifyResponseBodyFilter
//                .apply(new ModifyResponseBodyGatewayFilterFactory
//                        .Config()
//                        .setRewriteFunction(String.class, String.class, new ResponseBodyRewrite(objectMapper)))
//                .filter(exchange, chain));



        exchange.getAttributes().put(REQUEST_TIME_BEGIN, System.currentTimeMillis());

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(REQUEST_TIME_BEGIN);
                    if (startTime != null) {
                        log.info(exchange.getRequest().getURI().getRawPath() + ": " + (System.currentTimeMillis() - startTime) + "ms");
                    }
                })
        );
    }
}
