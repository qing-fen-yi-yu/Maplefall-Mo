package com.guilu.manager.register;

import com.guilu.annotation.MFTask;
import com.guilu.domain.DispatchMFTask;
import com.guilu.manager.TaskManager;
import com.guilu.quartz.MFTaskQuartzJob;
import com.guilu.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class MFTaskRegister extends AbstractRegister implements BeanPostProcessor {
    private static final String JOB_GROUP = "fengluo-mf-task";
    private final ApplicationContext applicationContext;
    private final TaskManager taskManager;
    private final Scheduler defaultScheduler;

    public MFTaskRegister(ApplicationContext applicationContext, TaskManager taskManager, Scheduler scheduler) {
        this.applicationContext = applicationContext;
        this.taskManager = taskManager;
        this.defaultScheduler = scheduler;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> targetClass = getBeanClass(bean);
        List<Method> methods = validAndGetVerifiedMethod(getBeanMethods(targetClass));
        for (Method method : methods) {
            MFTask annotation = AnnotatedElementUtils.findMergedAnnotation(method, MFTask.class);
            if (!ObjectUtils.isEmpty(annotation)) {
                register(beanName, annotation, method, bean.getClass(), annotation.name());
            }
        }
        return bean;
    }

    @Override
    public boolean register(String beanName, Object anno, Method method, Class<?> beanClass, String name) {
        MFTask annotation = (MFTask) anno;
        String taskId = validAndGetId(name, anno, method);
        if (method.getParameterCount() != 0) {
            log.warn("MFTask 方法不支持参数: {}#{}", beanClass.getName(), method.getName());
            return false;
        }

        String schedulerName = applicationContext.getEnvironment().resolvePlaceholders(annotation.scheduler()).trim();
        Scheduler scheduler = schedulerName.isEmpty()
                ? defaultScheduler : applicationContext.getBean(schedulerName, Scheduler.class);
        DispatchMFTask task;
        try {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, beanClass);
            task = new DispatchMFTask(beanName, invocableMethod, taskId, new Object[0], applicationContext);
        } catch (IllegalArgumentException e) {
            log.error("MFTask 方法无法通过 Spring Bean 调用: {}#{}", beanClass.getName(), method.getName(), e);
            return false;
        }

        String cron = applicationContext.getEnvironment().resolvePlaceholders(annotation.cron()).trim();
        String triggerName = applicationContext.getEnvironment().resolvePlaceholders(annotation.trigger()).trim();
        if (!cron.isEmpty() && !triggerName.isEmpty()) {
            log.error("MFTask cron 与 trigger 不能同时配置: {}", taskId);
            return false;
        }

        JobKey jobKey = JobKey.jobKey(taskId, JOB_GROUP);
        boolean hasQuartzTrigger = !cron.isEmpty() || !triggerName.isEmpty();
        if (hasQuartzTrigger) task.setQuartzSchedule(scheduler, jobKey);
        if (!taskManager.toRegistry(task)) {
            log.warn("MFTask ID 已存在，跳过注册: {}", taskId);
            return false;
        }

        try {
            if (hasQuartzTrigger) {
                JobDetail job = JobBuilder.newJob(MFTaskQuartzJob.class)
                        .withIdentity(jobKey)
                        .usingJobData(MFTaskQuartzJob.TASK_ID, taskId)
                        .storeDurably()
                        .build();
                Trigger trigger = triggerName.isEmpty()
                        ? TriggerBuilder.newTrigger()
                            .withIdentity(taskId + "-trigger", JOB_GROUP)
                            .forJob(job)
                            .withSchedule(cronSchedule(cron, annotation))
                            .build()
                        : applicationContext.getBean(triggerName, Trigger.class).getTriggerBuilder()
                            .withIdentity(taskId + "-trigger", JOB_GROUP)
                            .forJob(job)
                            .build();

                if (scheduler.checkExists(jobKey)) scheduler.deleteJob(jobKey);
                scheduler.scheduleJob(job, trigger);
            }
            return true;
        } catch (Exception e) {
            taskManager.removeMFTaskByName(taskId);
            log.error("注册 Quartz MFTask 失败: {}", taskId, e);
            return false;
        }
    }

    private static org.quartz.ScheduleBuilder<?> cronSchedule(String cron, MFTask annotation) {
        CronScheduleBuilder builder = CronScheduleBuilder.cronSchedule(cron);
        return switch (annotation.mistype()) {
            case MISFIRE_INSTRUCTION_DO_NOTHING,
                 MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT ->
                    builder.withMisfireHandlingInstructionDoNothing();
            case MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY ->
                    builder.withMisfireHandlingInstructionIgnoreMisfires();
            case MISFIRE_INSTRUCTION_FIRE_NOW,
                 MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT ->
                    builder.withMisfireHandlingInstructionFireAndProceed();
        };
    }
}
