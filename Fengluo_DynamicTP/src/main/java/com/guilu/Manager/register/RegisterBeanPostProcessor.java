package com.guilu.Manager.register;

import com.guilu.DynamicThreadPool.DynamicPool;
import com.guilu.annotation.DynamicThreadPool;
import com.guilu.domain.RegisPoolDto;
import com.guilu.utils.RegisDefaultNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Slf4j
public class RegisterBeanPostProcessor implements BeanPostProcessor {

    private final boolean enabled;
    private final ConfigurableListableBeanFactory beanFactory;
    public RegisterBeanPostProcessor(Environment environment, ConfigurableListableBeanFactory  beanFactory) {
        this.enabled = environment.getProperty(
                "monitor.threadpool.dynamic.enabled",
                Boolean.class,
                false);
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!enabled) {
            return bean;
        }
        Class<?> beanClass = AopUtils.getTargetClass(bean);
        List<RegisPoolDto> waitsRegisTP = new ArrayList<>();
        // 避免扫描线程池实现类的内部字段和方法
        if (isPool(bean)) {
            DynamicThreadPool annotation = findAnnotation(bean,beanName);
                if(annotation != null){
                    String poolName = resolveBeanPoolName(bean, beanName, annotation);
                    if (!poolName.equals(beanName) && beanFactory.containsBean(poolName)
                            && !Arrays.asList(beanFactory.getAliases(beanName)).contains(poolName)) {
                        throw new IllegalStateException("Pool name conflicts with Spring Bean: " + poolName);
                    }
                    if(!PoolRegister.registerThreadPool(bean, null, poolName)){
                        log.error("字段线程池扫描失败: bean={}", beanName);
                    } else if (!poolName.equals(beanName)) {
                        beanFactory.registerAlias(beanName, poolName);
                    }
                }
            return bean;
        }
        Object target = getTargetObject(bean);
        if (target == null) {
            return bean;
        }
        // 1. 扫描字段上的注解
        ReflectionUtils.doWithFields(beanClass, field -> {
            List<RegisPoolDto> regisPoolDtos = handleField(beanClass, field, target, beanName);
            if(regisPoolDtos != null && !regisPoolDtos.isEmpty()) {
                waitsRegisTP.addAll(regisPoolDtos);
            }
        });
        /*
        2. 扫描方法上的注解----可构建条件
        构造参数为空 & 没有通过@Bean声名由spring管理的 & 值为线程池类型的*/
        ReflectionUtils.doWithMethods(beanClass, method -> {
            List<RegisPoolDto> regisPoolDtos = handleMethod(beanClass, method, target, beanName);
            if(regisPoolDtos != null && !regisPoolDtos.isEmpty()) {
                waitsRegisTP.addAll(regisPoolDtos);
            }
        });
        // 3. 同步注册，确保 Bean 初始化完成后即可查询和调参。
        if (!waitsRegisTP.isEmpty()) {
            PoolRegister.registerThreadPool(waitsRegisTP);
        }

        return bean;
    }

    private boolean isPool(Object bean) {
        return bean instanceof ExecutorService
                || bean instanceof ThreadPoolTaskExecutor;
    }

    private String resolveBeanPoolName(Object bean, String beanName, DynamicThreadPool annotation) {
        if (beanFactory.containsBeanDefinition(beanName)) {
            var definition = beanFactory.getBeanDefinition(beanName);
            if (definition instanceof org.springframework.beans.factory.annotation.AnnotatedBeanDefinition annotated
                    && annotated.getFactoryMethodMetadata() != null) {
                MergedAnnotation<Bean> beanAnnotation = annotated.getFactoryMethodMetadata().getAnnotations().get(Bean.class);
                if (beanAnnotation.isPresent()) {
                    for (String name : beanAnnotation.synthesize().name()) {
                        if (name != null && !name.isBlank()) return name;
                    }
                }
            } else {
                return beanName;
            }
        }
        if (annotation.name() != null && !annotation.name().isBlank()) return annotation.name();
        if (bean instanceof DynamicPool pool
                && pool.getThreadPoolName() != null && !pool.getThreadPoolName().isBlank()) {
            return pool.getThreadPoolName();
        }
        return RegisDefaultNameUtil.createThreadPoolName();
    }

    /**
     * 获取 AOP 代理的实际目标对象
     */
    private Object getTargetObject(Object bean) {
        if (!(bean instanceof Advised advised)) {
            return bean;
        }

        try {
            // 只处理静态 TargetSource
            if (!advised.getTargetSource().isStatic()) {
                return null;
            }

            return advised.getTargetSource().getTarget();

        } catch (Exception e) {
            log.warn("获取代理目标对象失败", e);
            return null;
        }
    }
    private DynamicThreadPool findAnnotation(Object bean,String beanName){
        //优先从BeanFactory获取
        DynamicThreadPool anno = beanFactory.findAnnotationOnBean(beanName, DynamicThreadPool.class);
        if(anno != null){
            return anno;
        }
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        return AnnotationUtils.findAnnotation(targetClass, DynamicThreadPool.class);
    }
    private List<RegisPoolDto> handleField(Class<?> beanClass
            , Field field,Object target,String beanName){
        List<RegisPoolDto> waitsRegisTP = new ArrayList<>();
        DynamicThreadPool annotation = AnnotationUtils.findAnnotation(field, DynamicThreadPool.class);
        if (annotation == null) {
            return null;
        }
        try {
            ReflectionUtils.makeAccessible(field);
            Object value = field.get(target);
            if (isPool(value)) {
                waitsRegisTP.add(RegisPoolDto.of(value, annotation,beanName + "." + field.getName()));
            } else {
                log.warn("字段 {} 不是线程池，跳过注册", field.getName());
            }
        } catch (Exception e) {
            log.error("字段线程池---扫描失败: bean={}, field={}", beanName, field.getName(), e);
        }
        return waitsRegisTP;
    }
    private List<RegisPoolDto> handleMethod(Class<?> beanClass, Method method, Object target,
                                            String beanName){
        List<RegisPoolDto> waitsRegisTP = new ArrayList<>();
        DynamicThreadPool annotation = AnnotationUtils.findAnnotation(method, DynamicThreadPool.class);
        if (annotation == null) {
            return null;
        }
        if (AnnotationUtils.findAnnotation(method, Bean.class) != null) {
            return null;
        }
        if (method.getParameterCount() != 0) {
            return null;
        }
        if (!Executor.class.isAssignableFrom(
                method.getReturnType())) {
            return null;
        }
        try {
            ReflectionUtils.makeAccessible(method);
            Object invokeTarget = Modifier.isStatic(method.getModifiers()) ? null : target;
            Object value = method.invoke(invokeTarget);
            if (isPool(value)) {
                waitsRegisTP.add(RegisPoolDto.of(value, annotation, beanName + "." + method.getName()));
            } else {
                log.warn("方法 {} 未返回有效线程池", method.getName());
            }
        } catch (Exception e) {
            log.error("方法线程池---扫描失败: bean={}, method={}", beanName, method.getName(), e);
        }
        return  waitsRegisTP;
    }
}