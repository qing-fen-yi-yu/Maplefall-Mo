package com.guilu.manager.register;

import com.guilu.domain.DispatchMFTask;
import com.guilu.domain.DispatchScheduledTask;
import com.guilu.manager.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.boot.convert.DurationStyle;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/** Registers Spring @Scheduled methods in TaskManager and executes them through TaskScheduler. */
@Slf4j
public class ScheduledTaskRegister extends AbstractRegister implements BeanPostProcessor {
    private final ApplicationContext applicationContext;
    private final TaskManager taskManager;
    private final TaskScheduler taskScheduler;

    public ScheduledTaskRegister(ApplicationContext applicationContext, TaskManager taskManager,
                                  TaskScheduler taskScheduler) {
        this.applicationContext = applicationContext;
        this.taskManager = taskManager;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
            throws BeansException {
        Class<?> targetClass = getBeanClass(bean);
        List<Method> methods = validAndGetVerifiedMethod(getBeanMethods(targetClass));
        for (Method method : methods) {
            Scheduled annotation = AnnotatedElementUtils.findMergedAnnotation(method, Scheduled.class);
            if (annotation != null) {
                register(beanName, annotation, method, bean.getClass(), null);
            }
        }
        return bean;
    }

    @Override
    public boolean register(String beanName, Object anno, Method method, Class<?> invocableType, String name) {
        Scheduled scheduled = (Scheduled) anno;
        String taskId = validAndGetId(name, anno, method);
        if (method.getParameterCount() != 0) {
            log.warn("@Scheduled 方法不支持参数: {}#{}", method.getDeclaringClass().getName(), method.getName());
            return false;
        }

        try {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, invocableType);
            DispatchMFTask invocation = new DispatchMFTask(beanName, invocableMethod, taskId,
                    new Object[0], applicationContext);
            var future = schedule(scheduled, invocation);
            if (future == null) return false;

            DispatchScheduledTask task = new DispatchScheduledTask(invocation, taskId);
            task.setFuture(future);
            if (!taskManager.toRegistry(task)) {
                task.cancel();
                log.warn("@Scheduled task ID 已存在，跳过注册: {}", taskId);
                return false;
            }
            return true;
        } catch (RuntimeException e) {
            log.error("注册 @Scheduled 失败: {}#{}", method.getDeclaringClass().getName(), method.getName(), e);
            return false;
        }
    }

    private java.util.concurrent.ScheduledFuture<?> schedule(Scheduled scheduled, Runnable task) {
        String schedulerName = resolve(scheduled.scheduler());
        TaskScheduler scheduler = schedulerName.isBlank()
                ? taskScheduler : applicationContext.getBean(schedulerName, TaskScheduler.class);
        String cron = resolve(scheduled.cron());
        String fixedRate = resolve(scheduled.fixedRateString());
        String fixedDelay = resolve(scheduled.fixedDelayString());
        boolean hasRate = !fixedRate.isBlank() || scheduled.fixedRate() > 0;
        boolean hasDelay = !fixedDelay.isBlank() || scheduled.fixedDelay() > 0;
        if (!cron.isBlank()) {
            if (hasRate || hasDelay) throw new IllegalArgumentException("@Scheduled cron cannot be combined with fixedRate/fixedDelay");
            if (Scheduled.CRON_DISABLED.equals(cron)) return null;
            String zoneName = resolve(scheduled.zone());
            TimeZone zone = zoneName.isBlank() ? TimeZone.getDefault() : TimeZone.getTimeZone(zoneName);
            return scheduler.schedule(task, new CronTrigger(cron, zone));
        }
        if (hasRate && hasDelay) throw new IllegalArgumentException("@Scheduled fixedRate and fixedDelay are mutually exclusive");

        Duration initialDelay = initialDelay(scheduled);
        if (!fixedRate.isBlank()) {
            Duration period = duration(fixedRate, scheduled.timeUnit());
            return scheduler.scheduleAtFixedRate(task, Instant.now().plus(initialDelay), positive(period, "fixedRate"));
        }
        if (scheduled.fixedRate() > 0) {
            Duration period = Duration.of(scheduled.fixedRate(), scheduled.timeUnit().toChronoUnit());
            return scheduler.scheduleAtFixedRate(task, Instant.now().plus(initialDelay), positive(period, "fixedRate"));
        }

        if (!fixedDelay.isBlank()) {
            Duration delay = duration(fixedDelay, scheduled.timeUnit());
            return scheduler.scheduleWithFixedDelay(task, Instant.now().plus(initialDelay), positive(delay, "fixedDelay"));
        }
        if (scheduled.fixedDelay() > 0) {
            Duration delay = Duration.of(scheduled.fixedDelay(), scheduled.timeUnit().toChronoUnit());
            return scheduler.scheduleWithFixedDelay(task, Instant.now().plus(initialDelay), positive(delay, "fixedDelay"));
        }
        throw new IllegalArgumentException("@Scheduled 必须配置 cron、fixedRate 或 fixedDelay");
    }

    private Duration initialDelay(Scheduled scheduled) {
        String initialDelay = resolve(scheduled.initialDelayString());
        if (!initialDelay.isBlank()) {
            return Duration.ofMillis(Math.max(0L,
                    positiveOrZero(duration(initialDelay, scheduled.timeUnit()), "initialDelay").toMillis()));
        }
        return Duration.of(Math.max(0L, scheduled.initialDelay()), scheduled.timeUnit().toChronoUnit());
    }

    private String resolve(String value) {
        return applicationContext.getEnvironment().resolvePlaceholders(value);
    }

    private static Duration duration(String value, TimeUnit defaultUnit) {
        String trimmed = value.trim();
        if (trimmed.matches("[+-]?\\d+")) {
            return Duration.of(Long.parseLong(trimmed), defaultUnit.toChronoUnit());
        }
        return DurationStyle.detectAndParse(trimmed);
    }

    private static Duration positive(Duration duration, String attribute) {
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException(attribute + " must be greater than zero");
        }
        return duration;
    }

    private static Duration positiveOrZero(Duration duration, String attribute) {
        if (duration.isNegative()) throw new IllegalArgumentException(attribute + " must not be negative");
        return duration;
    }
}
