package com.bolingcavalry.changebody.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "user-id字段不能为空")
public class MyGatewayException extends Exception {
}

