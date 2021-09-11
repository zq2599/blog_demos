package com.bolingcavalry.changebody.handler;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

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

//        String code =

        getError(request);

//
//
//        if (error instanceof ResponseStatusException) {
//            return ((ResponseStatusException) error).getStatus();
//        }
//        return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);





        Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
        return ServerResponse.status(getHttpStatus(error)).contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(error));
    }
}
