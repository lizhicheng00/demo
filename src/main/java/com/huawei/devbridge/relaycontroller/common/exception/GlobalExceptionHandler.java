package com.huawei.devbridge.relaycontroller.common.exception;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException exception) {
        log.warn("Business exception: code={}, message={}",
                exception.getErrorCode().getCode(), exception.getMessage());
        return Result.failure(exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Request validation failed: {}", message);
        return Result.failure(ErrorCode.PARAM_INVALID, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException exception) {
        log.warn("Request constraint violation: {}", exception.getMessage());
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Void> handleMissingRequestHeader(MissingRequestHeaderException exception) {
        if ("X-User-Id".equalsIgnoreCase(exception.getHeaderName())) {
            log.warn("Missing request header: {}", exception.getHeaderName());
            return Result.failure(ErrorCode.UNAUTHORIZED, "X-User-Id is required");
        }
        log.warn("Missing request header: {}", exception.getHeaderName());
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingRequestParameter(MissingServletRequestParameterException exception) {
        log.warn("Missing request parameter: {}", exception.getParameterName());
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException exception) {
        String message = messageOf(exception, ErrorCode.PARAM_INVALID);
        log.warn("Request body not readable: {}", message);
        return Result.failure(ErrorCode.PARAM_INVALID, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return Result.failure(ErrorCode.INTERNAL_ERROR, messageOf(exception, ErrorCode.INTERNAL_ERROR));
    }

    private String messageOf(Exception exception, ErrorCode fallback) {
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(exception);
        String message = cause == null ? exception.getMessage() : cause.getMessage();
        return message == null ? fallback.getMessage() : message;
    }
}
