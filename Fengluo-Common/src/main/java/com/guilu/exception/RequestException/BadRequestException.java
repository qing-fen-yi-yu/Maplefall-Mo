package com.guilu.exception.RequestException;

import com.guilu.constants.ExceptionConstants;
import com.guilu.exception.GlobalException;

public class BadRequestException extends GlobalException {
    public BadRequestException(String message) {
        super(message);
    }
    public BadRequestException(Integer code, String message) {
        super(message, code);
    }
    public BadRequestException(){
        super(ExceptionConstants.MESSAGE.REQUESTEXCEPTION);
    }
}
