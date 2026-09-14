package com.guilu.exception.RequestException;

import com.guilu.exception.GlobalException;

public class UnauthorizedException extends GlobalException {
    public UnauthorizedException(String message) {
        super(message);
    }
    public UnauthorizedException(String message, Integer code) {
        super(message, code);
    }
}
