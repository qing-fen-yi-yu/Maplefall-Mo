package com.guilu.config;

import com.guilu.manager.TaskManager;
import com.guilu.manager.register.MFTaskRegister;
import com.guilu.manager.register.ScheduledTaskRegister;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(prefix = "task.dispatch", name = "enabled",
        havingValue = "true", matchIfMissing = true)   // 默认开启
public class TaskDispatchConfig {

    @Bean
    @ConditionalOnMissingBean
    public TaskManager taskManager() {
        return new TaskManager();
    }

    @Bean
    @ConditionalOnExpression(
            "#{environment.getProperty('task.dispatch.type','MFTASK').contains('MFTASK')}")
    public MFTaskRegister mfTaskRegister() {
        return new MFTaskRegister();
    }

    @Bean
    @ConditionalOnExpression(
            "#{environment.getProperty('task.dispatch.type','MFTASK').contains('SPRINGSCHEDULED')}")
    public ScheduledTaskRegister scheduledTaskRegister() {
        return new ScheduledTaskRegister();
    }
}