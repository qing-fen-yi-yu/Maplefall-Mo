package com.guilu.manager.register;

import com.guilu.domain.MFTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public class MFTaskRegister extends AbstractRegister{
    private static final MFTaskRegister REGISTER = new MFTaskRegister();
    private MFTaskRegister(){}
    public static  MFTaskRegister of(){
        return  REGISTER;
    }

    @Override
    public boolean register(Object anno,Method method,Class<?> beanClass , String name) {
        com.guilu.annotation.MFTask taskAnno = (com.guilu.annotation.MFTask) anno;
        name = validAndGetId(name,anno,method); //校验并得到TaskId
        Method invocableMethod = AopUtils.selectInvocableMethod(method, beanClass);
        ReflectionUtils.makeAccessible(invocableMethod);
        MFTask regisTask =
                new MFTask(beanClass,invocableMethod,name, Arrays.stream(invocableMethod.getParameters()).toList());
        boolean registry = toRegistry(regisTask);
        if(registry){
            return true;
        }
        log.warn("注册失败");
        return false;
    }
}
