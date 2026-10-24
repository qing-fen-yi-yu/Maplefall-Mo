package com.guilu.jvm;

import com.guilu.MXBean;

import java.lang.management.ClassLoadingMXBean;

public class ClassloadingMxBeanMetrics {
    private static final ClassLoadingMXBean classLoadingBean = MXBean.getClassLoadingBean();
    //加载的类总数
    public static Integer getLoadedClassCount(){
        return classLoadingBean.getLoadedClassCount();
    }
    //类的总加载书
    public static Long totalLoadedClassCount(){
        return classLoadingBean.getTotalLoadedClassCount();
    }
    //卸载的加载数
    public static Long unLoadedClassCount(){
        return classLoadingBean.getUnloadedClassCount();
    }
}
