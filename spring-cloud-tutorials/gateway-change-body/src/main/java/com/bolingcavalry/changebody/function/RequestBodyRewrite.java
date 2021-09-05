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
public class RequestBodyRewrite implements RewriteFunction<String, String> {

    private ObjectMapper objectMapper;

    public RequestBodyRewrite(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 根据用户ID获取用户名称的方法，可以按实际情况来内部实现，例如查库或缓存，或者远程调用
     * @param userId
     * @return
     */
    private  String mockUserName(int userId) {
        return "user-" + userId;
    }

    @Override
    public Publisher<String> apply(ServerWebExchange exchange, String body) {
        try {
            Map<String, Object> map = objectMapper.readValue(body, Map.class);

            // 取得id
            int userId = (Integer)map.get("user-id");

            // 得到nanme后写入map
            map.put("user-name", mockUserName(userId));

            return Mono.just(objectMapper.writeValueAsString(map));
        } catch (Exception ex) {
            log.error("1. json process fail", ex);
            return Mono.error(new Exception("1. json process fail", ex));
        }
    }
}