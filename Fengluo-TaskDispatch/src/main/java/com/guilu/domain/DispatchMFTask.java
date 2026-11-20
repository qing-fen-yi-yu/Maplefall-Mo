package com.guilu.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Getter
@Setter
public class DispatchMFTask extends Task implements Runnable {

    private final String beanName;
    private final Method method;
    private final Object[] args;

    @Nullable
    private volatile Scheduler scheduler;
    @Nullable
    private volatile JobKey jobKey;

    private final ApplicationContext applicationContext;

    public DispatchMFTask(String beanName, Method method, String taskId,
                          Object[] args, ApplicationContext applicationContext) {
        super(taskId);
        this.beanName = beanName;
        this.method = method;
        this.args = args == null ? new Object[0] : args.clone();
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        try {
            Object bean = applicationContext.getBean(beanName);
            Method invocable = AopUtils.selectInvocableMethod(method, bean.getClass());
            ReflectionUtils.makeAccessible(invocable);
            invocable.invoke(bean, args.clone());
        } catch (InvocationTargetException e) {
            log.error("任务执行业务异常 taskId={}, target={}#{}",
                    getTaskId(), method.getDeclaringClass().getSimpleName(), method.getName(),
                    e.getTargetException());
        } catch (Exception e) {
            log.error("任务调度异常 taskId={}, target={}#{}",
                    getTaskId(), method.getDeclaringClass().getSimpleName(), method.getName(), e);
        }
    }

    public void setQuartzSchedule(Scheduler scheduler, JobKey jobKey) {
        this.scheduler = scheduler;
        this.jobKey = jobKey;
    }

    public void cancel() {
        if (scheduler == null || jobKey == null) return;
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            log.error("取消 Quartz 任务失败 taskId={}", getTaskId(), e);
        }
    }
}
