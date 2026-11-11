package com.example.assetperform.common.exception;

import com.example.assetperform.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. 业务异常处理
    @ExceptionHandler(BusinessException.class)
    public <T> Result<T> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    // 2. JSR-303 参数校验异常处理（拦截 @Valid 产生的错误）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public <T> Result<T> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验未通过";
        log.warn("参数校验异常: {}", message);
        return Result.error(400, message);
    }

    // 3. 全局未知异常兜底处理
    @ExceptionHandler(Exception.class)
    public <T> Result<T> handleException(Exception e) {
        log.error("系统未知异常: ", e);
        return Result.error(500, "系统异常，请稍后再试");
    }
}
