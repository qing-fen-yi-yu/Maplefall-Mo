package com.guilu.exception.BusinessException;

import com.guilu.exception.GlobalException;

public class CommonException extends GlobalException {
    public CommonException(String message) {
        super(message);
    }
    public CommonException(String message, Integer code) {
        super(message,code);
    }
}
