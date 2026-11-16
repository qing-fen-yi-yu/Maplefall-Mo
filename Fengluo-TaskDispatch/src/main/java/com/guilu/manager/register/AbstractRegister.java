package com.guilu.manager.register;
import com.guilu.annotation.MFTask;
import com.guilu.manager.TaskManager;

import java.lang.reflect.Method;

public abstract class AbstractRegister extends TaskManager {
    public abstract boolean register(Object anno, Method method,Class<?> beanClass,String name);
    public static String getTaskId(Object taskAnno,Method method){
        if(taskAnno instanceof MFTask mfTaskAnno){
            String name = mfTaskAnno.name();
            if(name != null && !name.isBlank()){
                return name;
            }
        }
        return "%s-%s".formatted(taskAnno.getClass().getSimpleName(),method.getName());
    }
    protected String validAndGetId(String name,Object taskAnno,Method method){
        if(name == null || name.isBlank()){
            return  getTaskId(taskAnno,method);
        }
        return name;
    }
}
