package com.guilu.exception.BusinessException;

import com.guilu.exception.GlobalException;

public class UnLoginException extends GlobalException {
    public UnLoginException(String message) {
        super(message);
    }
    public UnLoginException(String message, Integer code) {
        super(message, code);
    }
}
