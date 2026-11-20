package com.guilu.config;

import com.guilu.manager.TaskManager;
import com.guilu.manager.register.MFTaskRegister;
import com.guilu.manager.register.ScheduledTaskRegister;
import com.guilu.properties.TaskDispatchProperties;
import com.guilu.properties.TaskDispatchTypeCondition;
import com.guilu.quartz.MFTaskQuartzJob;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.quartz.Scheduler;

import java.util.Map;

@AutoConfiguration(after = QuartzAutoConfiguration.class)
@EnableConfigurationProperties(TaskDispatchProperties.class)
@ConditionalOnClass({TaskScheduler.class, Scheduler.class})
@ConditionalOnProperty(prefix = "task.dispatch", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class TaskDispatchConfig {

    @Bean
    @ConditionalOnMissingBean(TaskManager.class)
    public TaskManager taskManager() {
        return new TaskManager();
    }

    @Bean
    @Conditional(TaskDispatchTypeCondition.MFTask.class)
    @ConditionalOnMissingBean(MFTaskRegister.class)
    public MFTaskRegister mfTaskRegister(ApplicationContext applicationContext, TaskManager taskManager,
                                         Scheduler scheduler) {
        return new MFTaskRegister(applicationContext, taskManager, scheduler);
    }

    @Bean
    @Conditional(TaskDispatchTypeCondition.SpringScheduled.class)
    @ConditionalOnMissingBean(TaskScheduler.class)
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("task-spring-scheduled");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }

    @Bean
    @Conditional(TaskDispatchTypeCondition.SpringScheduled.class)
    @ConditionalOnMissingBean(ScheduledTaskRegister.class)
    public ScheduledTaskRegister scheduledTaskRegister(ApplicationContext applicationContext,
                                                       TaskManager taskManager, TaskScheduler taskScheduler) {
        return new ScheduledTaskRegister(applicationContext, taskManager, taskScheduler);
    }

    @Bean
    public SchedulerFactoryBeanCustomizer taskDispatchSchedulerContext(TaskManager taskManager) {
        return factory -> factory.setSchedulerContextAsMap(
                Map.of(MFTaskQuartzJob.TASK_MANAGER, taskManager));
    }
}