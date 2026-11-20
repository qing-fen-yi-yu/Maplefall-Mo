package com.guilu.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.util.concurrent.ScheduledFuture;

@EqualsAndHashCode(callSuper = true)
public class DispatchScheduledTask extends Task{
    private final DispatchMFTask delegate;
    @Getter
    @Setter
    @Nullable
    private volatile ScheduledFuture<?> future;

    public DispatchScheduledTask(DispatchMFTask delegate, String taskId) {
        super(taskId);
        this.delegate = delegate;
    }

    public DispatchMFTask getDelegate() { return delegate; }

    public boolean cancel() {
        ScheduledFuture<?> scheduledFuture = future;
        return scheduledFuture == null || scheduledFuture.cancel(false);
    }

    @Override
    public String toString() {
        return "ScheduledTask{" +
                "delegate=" + delegate +
                '}';
    }
}
