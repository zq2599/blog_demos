package com.bolingcavalry.circuitbreakergateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/28 9:33
 */
@Component
public class StatePrinterGatewayFilterFactory extends AbstractGatewayFilterFactory<Object>
{
    @Autowired
    ReactiveResilience4JCircuitBreakerFactory reactiveResilience4JCircuitBreakerFactory;

    @Override
    public String name() {
        return "CircuitBreakerStatePrinter";
    }

    @Override
    public GatewayFilter apply(Object config)
    {
        return new StatePrinterGatewayFilter(reactiveResilience4JCircuitBreakerFactory);
    }
}