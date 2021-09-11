package com.bolingcavalry.changebody.function;

import com.bolingcavalry.changebody.exception.CustomizeInfoException;
import com.bolingcavalry.changebody.exception.MyGatewayException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.HttpStatus;
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

            // 如果请求参数中不含user-id，就返回异常
            if (!map.containsKey("user-id")) {
                  // http返回码是500
//                return Mono.error(new Exception("user-id参数不存在"));
                  // http返回码和MyGatewayException的注解有关
//                return Mono.error(new MyGatewayException());

                CustomizeInfoException customizeInfoException = new CustomizeInfoException();
                // 这里返回406，您可以按照业务需要自行调整
                customizeInfoException.setHttpStatus(HttpStatus.NOT_ACCEPTABLE);

                // 这里按照业务需要自行设置code
                customizeInfoException.setCode("010020003");

                // 这里按照业务需要自行设置返回的message
                customizeInfoException.setMessage("请确保请求参数中的user-id字段是有效的");

                return Mono.error(customizeInfoException);
            }

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