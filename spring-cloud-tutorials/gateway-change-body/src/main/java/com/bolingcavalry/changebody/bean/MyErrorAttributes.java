package com.bolingcavalry.changebody.bean;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/9/2 8:32
 */
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

/**
 * Default implementation of {@link ErrorAttributes}. Provides the following attributes
 * when possible:
 * <ul>
 * <li>timestamp - The time that the errors were extracted</li>
 * <li>status - The status code</li>
 * <li>error - The error reason</li>
 * <li>exception - The class name of the root exception (if configured)</li>
 * <li>message - The exception message (if configured)</li>
 * <li>errors - Any {@link ObjectError}s from a {@link BindingResult} exception (if
 * configured)</li>
 * <li>trace - The exception stack trace (if configured)</li>
 * <li>path - The URL path when the exception was raised</li>
 * <li>requestId - Unique ID associated with the current request</li>
 * </ul>
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Michele Mancioppi
 * @author Scott Frederick
 * @see ErrorAttributes
 * @since 2.0.0
 */
public class MyErrorAttributes implements ErrorAttributes {

    private static final String ERROR_ATTRIBUTE = org.springframework.boot.web.reactive.error.DefaultErrorAttributes.class.getName() + ".ERROR";

    private final Boolean includeException;

    /**
     * Create a new {@link org.springframework.boot.web.reactive.error.DefaultErrorAttributes} instance.
     */
    public MyErrorAttributes() {
        this.includeException = null;
    }

    /**
     * Create a new {@link org.springframework.boot.web.reactive.error.DefaultErrorAttributes} instance.
     *
     * @param includeException whether to include the "exception" attribute
     * @deprecated since 2.3.0 in favor of
     * {@link ErrorAttributeOptions#including(ErrorAttributeOptions.Include...)}
     */
    @Deprecated
    public MyErrorAttributes(boolean includeException) {
        this.includeException = includeException;
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = getErrorAttributes(request, options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE));
        if (Boolean.TRUE.equals(this.includeException)) {
            options = options.including(ErrorAttributeOptions.Include.EXCEPTION);
        }
        if (!options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION)) {
            errorAttributes.remove("exception");
        }
        if (!options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
            errorAttributes.remove("trace");
        }
        if (!options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) && errorAttributes.get("message") != null) {
            errorAttributes.put("message", "");
        }
        if (!options.isIncluded(ErrorAttributeOptions.Include.BINDING_ERRORS)) {
            errorAttributes.remove("errors");
        }
        return errorAttributes;
    }

    @Override
    @Deprecated
    public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", new Date());
        errorAttributes.put("path", request.path());
        Throwable error = getError(request);
        MergedAnnotation<ResponseStatus> responseStatusAnnotation = MergedAnnotations
                .from(error.getClass(), MergedAnnotations.SearchStrategy.TYPE_HIERARCHY).get(ResponseStatus.class);
        HttpStatus errorStatus = determineHttpStatus(error, responseStatusAnnotation);
        errorAttributes.put("status", errorStatus.value());
        errorAttributes.put("status", 200);
        errorAttributes.put("error", errorStatus.getReasonPhrase());
        errorAttributes.put("message", determineMessage(error, responseStatusAnnotation));
        errorAttributes.put("requestId", request.exchange().getRequest().getId());
        handleException(errorAttributes, determineException(error), includeStackTrace);
        return errorAttributes;
    }

    private HttpStatus determineHttpStatus(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getStatus();
        }
        return responseStatusAnnotation.getValue("code", HttpStatus.class).orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String determineMessage(Throwable error, MergedAnnotation<ResponseStatus> responseStatusAnnotation) {
        if (error instanceof BindingResult) {
            return error.getMessage();
        }
        if (error instanceof ResponseStatusException) {
            return ((ResponseStatusException) error).getReason();
        }
        String reason = responseStatusAnnotation.getValue("reason", String.class).orElse("");
        if (StringUtils.hasText(reason)) {
            return reason;
        }
        return (error.getMessage() != null) ? error.getMessage() : "";
    }

    private Throwable determineException(Throwable error) {
        if (error instanceof ResponseStatusException) {
            return (error.getCause() != null) ? error.getCause() : error;
        }
        return error;
    }

    private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));
        stackTrace.flush();
        errorAttributes.put("trace", stackTrace.toString());
    }

    private void handleException(Map<String, Object> errorAttributes, Throwable error, boolean includeStackTrace) {
        errorAttributes.put("exception", error.getClass().getName());
        if (includeStackTrace) {
            addStackTrace(errorAttributes, error);
        }
        if (error instanceof BindingResult) {
            BindingResult result = (BindingResult) error;
            if (result.hasErrors()) {
                errorAttributes.put("errors", result.getAllErrors());
            }
        }
    }

    @Override
    public Throwable getError(ServerRequest request) {
        return (Throwable) request.attribute(ERROR_ATTRIBUTE)
                .orElseThrow(() -> new IllegalStateException("Missing exception attribute in ServerWebExchange"));
    }

    @Override
    public void storeErrorInformation(Throwable error, ServerWebExchange exchange) {
        exchange.getAttributes().putIfAbsent(ERROR_ATTRIBUTE, error);
    }

}
