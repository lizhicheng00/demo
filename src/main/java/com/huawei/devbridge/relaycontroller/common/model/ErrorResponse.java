package com.huawei.devbridge.relaycontroller.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.huawei.devbridge.relaycontroller.common.exception.ErrorCode;
import java.util.List;

public record ErrorResponse(ErrorBody error) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String target) {
        return new ErrorResponse(new ErrorBody(errorCode.getCode(), message, target, null));
    }

    public static ErrorResponse validation(String message, List<ErrorDetail> details) {
        return new ErrorResponse(new ErrorBody(ErrorCode.PARAM_INVALID.getCode(), message, null, details));
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record ErrorBody(String code, String message, String target, List<ErrorDetail> details) {
    }

    public record ErrorDetail(String code, String target, String message) {
    }
}
