package com.bolingcavalry.changebody.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/8/28 9:33
 */
@Component
public class ChangeRequestBodyFilterFactory extends AbstractGatewayFilterFactory<Object>
{
    @Autowired
    private ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String name() {
        return "ChangeRequestBody";
    }

    @Override
    public GatewayFilter apply(Object config)
    {
        return new ChangeRequestBodyFilter(modifyRequestBodyFilter, objectMapper);
    }
}