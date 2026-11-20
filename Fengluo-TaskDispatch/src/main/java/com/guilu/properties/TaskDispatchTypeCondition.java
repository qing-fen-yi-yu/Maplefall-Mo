package com.guilu.properties;

import com.guilu.enums.TaskDispatchType;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;

public abstract class TaskDispatchTypeCondition implements Condition {
    private final TaskDispatchType expected;

    TaskDispatchTypeCondition(TaskDispatchType expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        List<TaskDispatchType> configured = Binder.get(context.getEnvironment())
                .bind("task.dispatch.type", Bindable.listOf(TaskDispatchType.class))
                .orElse(List.of(TaskDispatchType.MFTASK));
        return configured.contains(expected);
    }

    public static final class MFTask extends TaskDispatchTypeCondition {
        public MFTask() { super(TaskDispatchType.MFTASK); }
    }

    public static final class SpringScheduled extends TaskDispatchTypeCondition {
        public SpringScheduled() { super(TaskDispatchType.SPRINGSCHEDULED); }
    }
}
