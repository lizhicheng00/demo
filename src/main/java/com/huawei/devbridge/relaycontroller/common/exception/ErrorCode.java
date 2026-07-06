package com.huawei.devbridge.relaycontroller.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS("0000", "success"),

    PARAM_INVALID("40000", "parameter invalid"),
    UNAUTHORIZED("40100", "unauthorized"),
    FORBIDDEN("40300", "forbidden"),
    NOT_FOUND("40400", "not found"),

    GRID_NOT_FOUND("10001", "grid not found"),
    TUNNEL_NOT_FOUND("10002", "tunnel not found"),
    TUNNEL_ID_CONFLICT("10003", "tunnel id conflict"),
    TUNNEL_EXPIRED("10004", "tunnel expired"),
    TUNNEL_ACCESS_DENIED("10005", "tunnel access denied"),
    TUNNEL_PORT_INVALID("11001", "tunnel port invalid"),
    TUNNEL_PORT_ALREADY_EXISTS("11002", "tunnel port already exists"),
    TUNNEL_PORT_NOT_FOUND("11003", "tunnel port not found"),
    TUNNEL_PORT_ACCESS_DENIED("11004", "tunnel port access denied"),

    JWT_GENERATE_FAILED("30001", "jwt generate failed"),
    JWT_KEY_INVALID("30002", "jwt key invalid"),

    METERING_REPORT_FAILED("40001", "metering report failed"),

    INTERNAL_ERROR("50000", "internal error");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
