package com.guilu.manager.register;

import com.guilu.utils.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.util.List;

public class ScheduledTaskRegister extends AbstractRegister implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> beanClass = getBeanClass(bean);
        List<Method> verMtd = validAndGetVerifiedMethod(getBeanMethods(beanClass));
        verMtd.forEach(method -> {
            Scheduled annotation = method.getAnnotation(Scheduled.class);
            if(!ObjectUtils.isEmpty(annotation)){
                register(annotation, method, beanClass, null);
            }
        });
        return bean;
    }
    @Override
    public boolean register(Object anno, Method method, Class<?> beanClass,String name) {
        name = validAndGetId(name,anno,method);
        //todo
        return false;
    }
}
