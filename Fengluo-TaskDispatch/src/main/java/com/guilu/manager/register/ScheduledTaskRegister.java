package com.guilu.manager.register;

import com.guilu.domain.ScheduledTask;

import java.lang.reflect.Method;

public class ScheduledTaskRegister extends AbstractRegister{
    public static final ScheduledTaskRegister REGISTER = new ScheduledTaskRegister();
    private ScheduledTaskRegister(){}

    public static ScheduledTaskRegister of(){
        return  REGISTER;
    }
    @Override
    public boolean register(Object anno, Method method, Class<?> beanClass,String name) {
        name = validAndGetId(name,anno,method);
//        ScheduledTask scheduledTask = new ScheduledTask();
//        toRegistry(scheduledTask)
        return false;
    }
}
