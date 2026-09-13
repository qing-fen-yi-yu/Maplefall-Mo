package com.guilu.exception.BusinessException;

import com.guilu.exception.GlobalException;

/**
 * 网关异常类
 */
public class GateWayException extends GlobalException {
    public GateWayException(String message) {
        super(message);
    }
    public GateWayException(String message, Integer code) {
        super(message, code);
    }
}
