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
//public class StatePrinterGatewayFilter implements GatewayFilter, Ordered {
public class StatePrinterGatewayFilter implements GatewayFilter {

    private ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory;

    // 通过构造方法取得reactiveResilience4JCircuitBreakerFactory实例
    public StatePrinterGatewayFilter(ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory) {
        this.reactiveResilience4JCircuitBreakerFactory = reactiveResilience4JCircuitBreakerFactory;
    }

    private CircuitBreaker circuitBreaker = null;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 这里没有考虑并发的情况，如果是生产环境，请您自行添加上锁的逻辑
        if (null==circuitBreaker) {
            CircuitBreakerRegistry circuitBreakerRegistry = null;
            try {
                Method method = reactiveResilience4JCircuitBreakerFactory.getClass().getDeclaredMethod("getCircuitBreakerRegistry",(Class[]) null);
                // 用反射将getCircuitBreakerRegistry方法设置为可访问
                method.setAccessible(true);
                // 用反射执行getCircuitBreakerRegistry方法，得到circuitBreakerRegistry
                circuitBreakerRegistry = (CircuitBreakerRegistry)method.invoke(reactiveResilience4JCircuitBreakerFactory);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            // 得到所有断路器实例
            Seq<CircuitBreaker> seq = circuitBreakerRegistry.getAllCircuitBreakers();
            // 用名字过滤，myCircuitBreaker来自路由配置中
            circuitBreaker = seq.filter(breaker -> breaker.getName().equals("myCircuitBreaker"))
                    .getOrNull();
        }

        // 取断路器状态，再判空一次，因为上面的操作未必能取到circuitBreaker
        String state = (null==circuitBreaker) ? "unknown" : circuitBreaker.getState().name();

        System.out.println("state : " + state);

        // 继续执行后面的逻辑
        return chain.filter(exchange);
    }

//    @Override
//    public int getOrder() {
//        return 10;
//    }
}