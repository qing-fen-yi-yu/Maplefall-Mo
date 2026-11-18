package com.guilu.manager.register;

import com.guilu.annotation.MFTask;
import com.guilu.domain.DispatchMFTask;
import com.guilu.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class MFTaskRegister extends AbstractRegister implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        Class<?> beanClass = getBeanClass(bean);
        List<Method> verMtd = validAndGetVerifiedMethod(getBeanMethods(beanClass));
        verMtd.forEach(method -> {
            MFTask annotation = AnnotatedElementUtils.getMergedAnnotation(method,MFTask.class);
            if(!ObjectUtils.isEmpty(annotation)){
                log.info("mfTask{}",annotation.toString());
                register(annotation, method, beanClass, annotation.name());
            }
        });
        return bean;
    }
    @Override
    public boolean register(Object anno,Method method,Class<?> beanClass , String name) {
        MFTask taskAnno = (MFTask) anno;
        name = validAndGetId(name,anno,method); //校验并得到TaskId
        Method invocableMethod = AopUtils.selectInvocableMethod(method, beanClass);
        ReflectionUtils.makeAccessible(invocableMethod);
        DispatchMFTask regisTask =
                new DispatchMFTask(beanClass,invocableMethod,name, Arrays.stream(invocableMethod.getParameters()).toList());
        log.info("注册 Task Id --- {}",name);
        return toRegistry(regisTask);
    }
}
