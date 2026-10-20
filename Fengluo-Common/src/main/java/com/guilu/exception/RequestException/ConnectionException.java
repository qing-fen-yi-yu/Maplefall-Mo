package com.guilu.exception.RequestException;

import com.guilu.exception.GlobalException;

public class ConnectionException extends GlobalException {
    public ConnectionException(String message) {
        super(message);
    }
    public ConnectionException(String message, Integer code) {
        super(message, code);
    }
    public ConnectionException(){
        super("连接异常");
    }
    public ConnectionException(Throwable cause) {
        super(cause.getMessage(),cause);

    }
}
