package com.guilu.threadPool.Register;

import com.guilu.threadPool.annotation.DynamicThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

//线程池扫描注册中心
@Slf4j
public class ThreadRegister implements BeanPostProcessor {

    private final Environment environment;
    private final ApplicationContext applicationContext;

    public ThreadRegister(Environment environment, ApplicationContext applicationContext) {
        this.environment = environment;
        this.applicationContext = applicationContext;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Boolean enabled = environment.getProperty(
                "monitor.threadpool.dynamic.enabled", Boolean.class, Boolean.FALSE);
        if (!enabled) {
            return bean;
        }
        Class<DynamicThreadPool> anno = DynamicThreadPool.class;
        Class<?> beanClass = AopUtils.getTargetClass(bean);
        //获取Bean上的所有字段、方法、类上的注解
        Field[] declaredFields = beanClass.getDeclaredFields();
        Method[] methods = beanClass.getMethods();
        for (Field field : declaredFields) {
            DynamicThreadPool annotation = field.getAnnotation(anno);
            if(annotation != null) {
                field.setAccessible(true);
                try {
                    Object value = field.get(bean);
                    log.info("扫描到类"+value);
                    regisToCache(value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (Method method : methods) {
            DynamicThreadPool annotation = method.getAnnotation(DynamicThreadPool.class);
            if (annotation != null){}
        }
        return bean;
    }
    private void regisToCache(Object pollValue){
        if(pollValue instanceof ThreadPoolExecutor){
        } else if(pollValue instanceof ExecutorService){
        }
    }
}
