package com.huawei.devbridge.relaycontroller.common.exception;

import com.huawei.devbridge.relaycontroller.common.model.Result;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.springframework.validation.BindException;
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

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.failure(ErrorCode.INTERNAL_ERROR, exception.getMessage());
    }
}
