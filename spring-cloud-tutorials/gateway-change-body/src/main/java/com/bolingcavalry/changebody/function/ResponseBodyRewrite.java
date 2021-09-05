package com.bolingcavalry.changebody.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author zq2599@gmail.com
 * @Title:
 * @Package
 * @Description:
 * @date 8/30/21 2:58 下午
 */
@Slf4j
public class ResponseBodyRewrite implements RewriteFunction<String, String> {

    private ObjectMapper objectMapper;

    public ResponseBodyRewrite(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Publisher<String> apply(ServerWebExchange exchange, String body) {
        try {
            Map<String, Object> map = objectMapper.readValue(body, Map.class);

            // 取得id
            int userId = (Integer)map.get("user-id");

            // 添加一个key/value
            map.put("gateway-response-tag", userId + "-" + System.currentTimeMillis());

            return Mono.just(objectMapper.writeValueAsString(map));
        } catch (Exception ex) {
            log.error("2. json process fail", ex);
            return Mono.error(new Exception("2. json process fail", ex));
        }
    }
}