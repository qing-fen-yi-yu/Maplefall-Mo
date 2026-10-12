package com.guilu.exception.BusinessException;

import com.guilu.exception.GlobalException;

public class SQLOptionalException extends GlobalException {
    public SQLOptionalException(String message) {
        super(message);
    }
    public  SQLOptionalException(String message, Throwable cause) {
        super(message, cause);
    }
    public SQLOptionalException(Integer code,String message){
        super(message,code);
    }
}
