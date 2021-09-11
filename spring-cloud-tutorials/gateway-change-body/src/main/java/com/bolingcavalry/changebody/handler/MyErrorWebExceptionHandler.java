package com.bolingcavalry.changebody.handler;

import com.bolingcavalry.changebody.exception.CustomizeInfoException;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 9/11/21 7:37 AM
 * @description 继承DefaultErrorWebExceptionHandler，重写生成响应代码的逻辑
 */
public class MyErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    public MyErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources, errorProperties, applicationContext);
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        // 返回码
        int status;
        // 最终是用responseBodyMap来生成响应body的
        Map<String, Object> responseBodyMap = new HashMap<>();

        // 这里和父类的做法一样，取得DefaultErrorAttributes整理出来的所有异常信息
        Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));

        // 原始的异常信息可以用getError方法取得
        Throwable throwable = getError(request);

        // 如果异常类是咱们定制的，就定制
        if (throwable instanceof CustomizeInfoException) {
            CustomizeInfoException myGatewayException = (CustomizeInfoException) throwable;
            // http返回码、body的code字段、body的message字段，这三个信息都从CustomizeInfoException实例中获取
            status = myGatewayException.getHttpStatus().value();
            responseBodyMap.put("code", myGatewayException.getCode());
            responseBodyMap.put("message", myGatewayException.getMessage());
            responseBodyMap.put("data", null);
        } else {
            // 如果不是咱们定制的异常，就维持和父类一样的逻辑
            // 返回码
            status = getHttpStatus(error);
            // body内容
            responseBodyMap.putAll(error);
        }

        return ServerResponse
                // http返回码
                .status(status)
                // 类型和以前一样
                .contentType(MediaType.APPLICATION_JSON)
                // 响应body的内容
                .body(BodyInserters.fromValue(responseBodyMap));
    }
}
