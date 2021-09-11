package com.bolingcavalry.changebody.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 9/11/21 8:01 AM
 * @description 功能介绍
 */
@Data
public class CustomizeInfoException extends Exception {

    /**
     * http返回码
     */
    private HttpStatus httpStatus;

    /**
     * body中的code字段(业务返回码)
     */
    private String code;

    /**
     * body中的message字段(业务返回信息)
     */
    private String message;

}
