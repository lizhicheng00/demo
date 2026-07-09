package com.huawei.devbridge.relaycontroller.common.exception;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.common.util.ExceptionUtils;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return handleBindException(exception);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Request validation failed: {}", message);
        return Result.failure(ErrorCode.PARAM_INVALID, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException exception) {
        log.warn("Request constraint violation: {}", ExceptionUtils.anonymousMessage(exception));
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Void> handleMissingRequestHeader(MissingRequestHeaderException exception) {
        log.warn("Missing request header: {}", exception.getHeaderName());
        if ("X-Namespace".equalsIgnoreCase(exception.getHeaderName())) {
            return Result.failure(ErrorCode.UNAUTHORIZED, "X-Namespace is required");
        }
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingRequestParameter(MissingServletRequestParameterException exception) {
        log.warn("Missing request parameter: {}", exception.getParameterName());
        return Result.failure(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException exception) {
        String message = messageOf(exception);
        log.warn("Request body not readable: {}", ExceptionUtils.anonymousMessage(exception));
        return Result.failure(ErrorCode.PARAM_INVALID, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException exception) {
        log.error("Unhandled exception: {}", ExceptionUtils.anonymousMessage(exception));
        return Result.failure(ErrorCode.INTERNAL_ERROR);
    }

    private static String messageOf(Throwable exception) {
        Throwable cause = rootCause(exception);
        String message = cause.getMessage();
        return message == null ? ErrorCode.PARAM_INVALID.getMessage() : message;
    }

    private static Throwable rootCause(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
