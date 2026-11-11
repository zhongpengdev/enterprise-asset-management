package com.example.assetperform.common.exception;

import lombok.Getter;

@Getter 
public class BusinessException extends RuntimeException {
    private Integer code;
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    public BusinessException(String message) {
        super(message);
        this.code = 500; // 默认内部错误500
    }
}
