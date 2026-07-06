package com.huawei.devbridge.relaycontroller.common.exception;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.validation.BindException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException exception) {
        return Result.failure(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Result.failure(ErrorCode.PARAM_INVALID, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException exception) {
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Void> handleMissingRequestHeader(MissingRequestHeaderException exception) {
        if ("X-User-Id".equalsIgnoreCase(exception.getHeaderName())) {
            return Result.failure(ErrorCode.UNAUTHORIZED, "X-User-Id is required");
        }
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingRequestParameter(MissingServletRequestParameterException exception) {
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException exception) {
        return Result.failure(ErrorCode.PARAM_INVALID, messageOf(exception, ErrorCode.PARAM_INVALID));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.failure(ErrorCode.INTERNAL_ERROR, messageOf(exception, ErrorCode.INTERNAL_ERROR));
    }

    private String messageOf(Exception exception, ErrorCode fallback) {
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(exception);
        String message = cause == null ? exception.getMessage() : cause.getMessage();
        return message == null ? fallback.getMessage() : message;
    }
}
