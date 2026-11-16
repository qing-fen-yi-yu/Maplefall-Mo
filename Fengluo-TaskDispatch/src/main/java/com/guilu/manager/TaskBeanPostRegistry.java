package com.guilu.manager;


import com.guilu.annotation.MFTask;
import com.guilu.constants.ExceptionConstants;
import com.guilu.exception.BusinessException.TaskException;
import com.guilu.manager.register.AbstractRegister;
import com.guilu.manager.register.MFTaskRegister;
import com.guilu.manager.register.ScheduledTaskRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

@Slf4j
public class TaskBeanPostRegistry implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean,@NonNull String beanName) throws BeansException {
        Class<?> beanClass = AopUtils.getTargetClass(bean);
        Method[] methods = beanClass.getMethods();
        for (Method method : methods) {
            MFTask taskAnno = method.getAnnotation(MFTask.class);
            Scheduled scheduledAnno = method.getAnnotation(Scheduled.class);
            if(taskAnno != null || scheduledAnno != null){
                if(taskAnno != null && scheduledAnno != null) {
                    //无法确定任务调度类型强制报错，退出编译
                    throw new TaskException(ExceptionConstants.REC_ANNO_EXCEPTION
                            +"\tERROR CLASS:"+beanClass.getName()+"ERROR METHOD:"+method.getName());
                }
                boolean res = false;
                if(taskAnno != null){
                    res = MFTaskRegister.of().register(taskAnno,method, beanClass,AbstractRegister.getTaskId(taskAnno,method));
                } else {
                    res = ScheduledTaskRegister.of()
                            .register(scheduledAnno,method, beanClass,AbstractRegister.getTaskId(scheduledAnno,method));
                }
                if(!res){
                    //未注册成功
                    log.error("{}任务未注册成功",method.getName());
                }
            }
        }
        return bean;
    }
}
