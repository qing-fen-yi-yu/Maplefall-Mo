package com.guilu.exception;

import lombok.Getter;

/**
 * 构建业务统一异常父类，并暴露指标
 */
@Getter
public class GlobalException extends RuntimeException {
    private Integer code;
    public GlobalException(String message) {
        super(message);
    }
    public GlobalException(String message, Integer code) {
        super(message);
        this.code = code;
    }
}
