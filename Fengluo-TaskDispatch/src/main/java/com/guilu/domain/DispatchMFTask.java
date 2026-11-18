package com.guilu.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Getter
@Setter
public class DispatchMFTask extends Task  implements Runnable{
    private final Class<?> clazz;
    private final Method method;
    private final List<Parameter> params;
    @Nullable
    private volatile ScheduledFuture<?> future;
    public DispatchMFTask(Class<?> clazz, Method method, String taskId, List<Parameter> params) {
        super(taskId);
        this.clazz = clazz;
        this.method = method;
        this.params = params;
    }

    @Override
    public void run() {
        //todo
    }

    @Override
    public String toString() {
        return "MFTask{" +
                "clazz=" + clazz +
                ", method=" + method +
                ", params=" + params +
                ", future=" + future +
                '}';
    }
}
