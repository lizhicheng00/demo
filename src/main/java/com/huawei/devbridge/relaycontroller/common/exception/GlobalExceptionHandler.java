package com.huawei.devbridge.relaycontroller.common.exception;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import com.huawei.devbridge.relaycontroller.common.util.ExceptionUtils;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException exception) {
        log.warn("Business exception: code={}, message={}",
                exception.getErrorCode().getCode(), exception.getMessage());
        return failure(statusOf(exception.getErrorCode()), exception.getErrorCode(), exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Void>> handleBindException(BindException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Request validation failed: {}", message);
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_INVALID, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        log.warn("Request constraint violation: {}", ExceptionUtils.anonymousMessage(exception));
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Result<Void>> handleMissingRequestHeader(MissingRequestHeaderException exception) {
        log.warn("Missing request header: {}", exception.getHeaderName());
        if ("X-Namespace".equalsIgnoreCase(exception.getHeaderName())) {
            return failure(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, "X-Namespace is required");
        }
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleMessageNotReadable(HttpMessageNotReadableException exception) {
        String message = messageOf(exception);
        log.warn("Request body not readable: {}", ExceptionUtils.anonymousMessage(exception));
        return failure(HttpStatus.BAD_REQUEST, ErrorCode.PARAM_INVALID, message);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException exception) {
        log.error("Unhandled exception: {}", ExceptionUtils.anonymousMessage(exception));
        return failure(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
    }

    private static ResponseEntity<Result<Void>> failure(HttpStatus status, ErrorCode errorCode) {
        return ResponseEntity.status(status).body(Result.failure(errorCode));
    }

    private static ResponseEntity<Result<Void>> failure(HttpStatus status, ErrorCode errorCode, String message) {
        return ResponseEntity.status(status).body(Result.failure(errorCode, message));
    }

    private static HttpStatus statusOf(ErrorCode errorCode) {
        return switch (errorCode) {
            case PARAM_INVALID, TUNNEL_PORT_INVALID, METERING_REPORT_FAILED -> HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case TUNNEL_ACCESS_DENIED, TUNNEL_PORT_ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case GRID_NOT_FOUND, TUNNEL_NOT_FOUND, TUNNEL_PORT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case TUNNEL_EXPIRED -> HttpStatus.GONE;
            case TUNNEL_ID_CONFLICT, TUNNEL_PORT_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case TUNNEL_QUOTA_EXCEEDED, RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case JWT_GENERATE_FAILED, JWT_KEY_INVALID, INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case SUCCESS -> HttpStatus.OK;
        };
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
