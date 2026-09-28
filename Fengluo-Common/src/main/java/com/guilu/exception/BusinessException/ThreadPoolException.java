package com.guilu.exception.BusinessException;

import com.guilu.exception.GlobalException;

public class ThreadPoolException extends GlobalException {

    public ThreadPoolException(String message) {
        super(message);
    }
    public  ThreadPoolException(String message, Integer code) {
        super(message, code);
    }
    public ThreadPoolException(){
        super("线程池异常");
    }
}
