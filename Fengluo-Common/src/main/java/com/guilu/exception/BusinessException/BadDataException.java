package com.guilu.exception.BusinessException;

import com.guilu.constants.ExceptionConstants;
import com.guilu.exception.GlobalException;

public class BadDataException extends GlobalException {
    public BadDataException(String message) {
        super(message);
    }
    public BadDataException(Integer code, String message) {
        super(message, code);
    }
    public BadDataException(){
        super(ExceptionConstants.MESSAGE.DATAEXCEPTION,10000);
    }
}
