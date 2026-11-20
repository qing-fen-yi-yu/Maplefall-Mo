package com.guilu.manager.register;
import com.guilu.annotation.MFTask;
import com.guilu.manager.TaskManager;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.util.*;

public abstract class AbstractRegister {
    public abstract boolean register(String beanName,Object anno, Method method,Class<?> beanClass,String name);
    protected String getTaskId(Object taskAnno,Method method){
        if(taskAnno instanceof MFTask mfTaskAnno){
            String name = mfTaskAnno.name();
            if(name != null && !name.isBlank()){
                return name;
            }
        }
        return "%s-%s".formatted(method.getName(),TaskManager.getTaskID());
    }
    protected String validAndGetId(String name,Object taskAnno,Method method){
        if(name == null || name.isBlank()){
            return getTaskId(taskAnno,method);
        }
        return name;
    }
    public List<Method> getVerifiedMethod(Object bean){
        Class<?> beanClass = getBeanClass(bean);
        return getVerifiedMethod(beanClass);
    }
    public List<Method> getVerifiedMethod(Class<?> beanClass){
        return validAndGetVerifiedMethod(beanClass.getMethods());
    }
    public List<Method> validAndGetVerifiedMethod(Collection<Method> methods){
        return validAndGetVerifiedMethod(methods.toArray(new Method[0]));
    }
    public List<Method> validAndGetVerifiedMethod(Method[] methods){
        List<Method> mS = new ArrayList<>();
        for (Method method : methods) {
            boolean valiMethod = validMethod(method);
            if(valiMethod) {
                mS.add(method);
            }
        }
        return mS;
    }
    public Class<?> getBeanClass(Object bean){
        return AopUtils.getTargetClass(bean);
    }
    public Method[] getBeanMethods(Class<?> bean){
        return bean.getMethods();
    }
    private boolean validMethod(Method method) {
        boolean hasMFTask = AnnotatedElementUtils.hasAnnotation(method, MFTask.class);
        boolean hasScheduled = AnnotatedElementUtils.hasAnnotation(method, Scheduled.class);
        return hasMFTask ^ hasScheduled;
    }
}
