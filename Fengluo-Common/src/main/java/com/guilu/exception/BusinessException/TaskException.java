package com.guilu.exception.BusinessException;

import com.guilu.exception.GlobalException;

public class TaskException extends GlobalException {
    public TaskException(String message) {
        super(message);
    }

    public TaskException(String message, Integer code) {
        super(message, code);
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }
    public TaskException() {
        super("任务调度区异常");
    }
}
