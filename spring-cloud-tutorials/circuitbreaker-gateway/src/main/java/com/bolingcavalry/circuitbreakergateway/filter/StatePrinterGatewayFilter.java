package com.bolingcavalry.circuitbreakergateway.filter;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.collection.Seq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/28 9:32
 */
public class StatePrinterGatewayFilter implements GatewayFilter, Ordered
{
    public StatePrinterGatewayFilter(ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory) {
        this.reactiveResilience4JCircuitBreakerFactory = reactiveResilience4JCircuitBreakerFactory;
    }

    private ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory;

    private CircuitBreaker circuitBreaker = null;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        if (null==circuitBreaker) {
            CircuitBreakerRegistry circuitBreakerRegistry = null;
            try {
                Method method = reactiveResilience4JCircuitBreakerFactory.getClass().getDeclaredMethod("getCircuitBreakerRegistry",(Class[]) null);
                method.setAccessible(true);
                circuitBreakerRegistry = (CircuitBreakerRegistry)method.invoke(reactiveResilience4JCircuitBreakerFactory);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            Seq<CircuitBreaker> seq = circuitBreakerRegistry.getAllCircuitBreakers();
            circuitBreaker = seq.filter(breaker -> breaker.getName().equals("myCircuitBreaker"))
                    .getOrNull();
        }

        // 去断路器状态，再判空一次，因为上面的操作未必能取到circuitBreaker
        String state = (null==circuitBreaker) ? "unknown" : circuitBreaker.getState().name();

        System.out.println("state : " + state);

        // 继续执行后面的逻辑
        return chain.filter(exchange);
    }

    @Override
    public int getOrder()
    {
        return 10;
    }
}