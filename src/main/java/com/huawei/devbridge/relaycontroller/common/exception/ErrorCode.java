package com.huawei.devbridge.relaycontroller.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(0, "success"),

    PARAM_INVALID(40000, "parameter invalid"),
    UNAUTHORIZED(40100, "unauthorized"),
    FORBIDDEN(40300, "forbidden"),
    NOT_FOUND(40400, "not found"),

    GRID_NOT_FOUND(10001, "grid not found"),
    TUNNEL_NOT_FOUND(10002, "tunnel not found"),
    TUNNEL_ID_CONFLICT(10003, "tunnel id conflict"),
    TUNNEL_EXPIRED(10004, "tunnel expired"),
    TUNNEL_ACCESS_DENIED(10005, "tunnel access denied"),

    NODE_NOT_FOUND(20001, "node not found"),
    NODE_ID_INVALID(20002, "node id invalid"),

    JWT_GENERATE_FAILED(30001, "jwt generate failed"),
    JWT_KEY_INVALID(30002, "jwt key invalid"),
    TOKEN_INVALID(30003, "token invalid"),
    TOKEN_NOT_FOUND_OR_EXPIRED(30004, "token not found or expired"),
    TOKEN_ALREADY_CONSUMED(30005, "token already consumed"),

    METERING_REPORT_FAILED(40001, "metering report failed"),

    INTERNAL_ERROR(50000, "internal error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
