package com.qq24650393.demo.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    BAD_REQUEST("COMMON_400", "request is invalid", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("AUTH_401", "authentication is required", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("AUTH_403", "permission denied", HttpStatus.FORBIDDEN),
    NOT_FOUND("COMMON_404", "resource not found", HttpStatus.NOT_FOUND),
    CONFLICT("COMMON_409", "resource already exists", HttpStatus.CONFLICT),
    VALIDATION_FAILED("COMMON_422", "validation failed", HttpStatus.UNPROCESSABLE_ENTITY),
    INTERNAL_ERROR("COMMON_500", "internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TOKEN("AUTH_1001", "token is invalid", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("AUTH_1002", "username or password is incorrect", HttpStatus.UNAUTHORIZED),
    RELAY_DOMAIN_NOT_FOUND("RELAY_1001", "relay domain not found", HttpStatus.NOT_FOUND),
    RELAY_DOMAIN_EXISTS("RELAY_1002", "relay domain already exists", HttpStatus.CONFLICT),
    NODE_NOT_FOUND("NODE_1001", "node not found", HttpStatus.NOT_FOUND),
    LISTENING_NOT_FOUND("LISTENING_1001", "listening config not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }

    public HttpStatus status() {
        return status;
    }
}
