package com.guilu;

import com.guilu.exception.RequestException.ConnectionException;
import lombok.extern.slf4j.Slf4j;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

@Slf4j
public class JMXBean {

    public static MBeanServerConnection getLocalMBeanServerConnection() {
        return ManagementFactory.getPlatformMBeanServer();
    }

    public static RuntimeMXBean getRunTimeBeanByFactory() {
        return ManagementFactory.getRuntimeMXBean();
    }

    public static RuntimeMXBean getRunTimeBeanByConnection(MBeanServerConnection conn) {
        return getMXBean(conn, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
    }

    /**
     * 获取本地平台 MXBean
     */
    public static <T> T getLocalMXBean(String beanName, Class<T> clazz) {
        return getMXBean(getLocalMBeanServerConnection(), beanName, clazz);
    }

    /**
     * 获取远程 MXBean
     */
    public static <T> T getRemoteMXBean(MBeanServerConnection conn, String beanName, Class<T> clazz) {
        return getMXBean(conn, beanName, clazz);
    }

    /**
     * 兼容原有方法名。返回类型改为泛型后
     */
    @Deprecated
    public static <T> T getCollection(MBeanServerConnection conn, String beanName, Class<T> clazz) {
        return getMXBean(conn, beanName, clazz);
    }

    /**
     * 通用 MXBean 代理创建（本地/远程共用）
     */
    public static <T> T getMXBean(MBeanServerConnection conn, String beanName, Class<T> clazz) {
        try {
            return ManagementFactory.newPlatformMXBeanProxy(conn, beanName, clazz);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    public static MemoryMXBean getMemoryBean() {
        return ManagementFactory.getMemoryMXBean();
    }
    public static ThreadMXBean getThreadBean() {
        return ManagementFactory.getThreadMXBean();
    }
    public static ClassLoadingMXBean getClassLoadingBean() {
        return ManagementFactory.getClassLoadingMXBean();
    }
    public static CompilationMXBean getCompilationBean() {
        return ManagementFactory.getCompilationMXBean();
    }
    public static OperatingSystemMXBean getOperatingSystemBean() {
        return ManagementFactory.getOperatingSystemMXBean();
    }
}