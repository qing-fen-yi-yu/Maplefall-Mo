package com.guilu.exception.RequestException;

import com.guilu.exception.GlobalException;

public class ForbiddenException extends GlobalException {
    public ForbiddenException(String message) {
        super(message);
    }
}
