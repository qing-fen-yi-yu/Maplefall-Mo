package com.guilu;

import com.guilu.exception.RequestException.ConnectionException;
import lombok.extern.slf4j.Slf4j;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.*;
import java.util.List;

@Slf4j
public class MXBean {

    public static MBeanServerConnection getLocalMBeanServerConnection() {
        return ManagementFactory.getPlatformMBeanServer();
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

    @Deprecated
    public static <T> T getCollection(MBeanServerConnection conn, String beanName, Class<T> clazz) {
        return getMXBean(conn, beanName, clazz);
    }

    /**
     * 通用 MXBean 代理创建
     */
    public static <T> T getMXBean(MBeanServerConnection conn, String beanName, Class<T> clazz) {
        try {
            return ManagementFactory.newPlatformMXBeanProxy(conn, beanName, clazz);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }

    /**
     * 获取本地所有类型相同的mxBean
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T extends PlatformManagedObject> List<T> getLocalAllTypeMxBean(Class<T> clazz){
        return getAllTypeMxBeans(getLocalMBeanServerConnection(), clazz);
    }
    public static <T extends  PlatformManagedObject> List<T> getAllTypeMxBeans(MBeanServerConnection connection,Class<T> clazz){
        try {
            return ManagementFactory.getPlatformMXBeans(connection,clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans(){
        return ManagementFactory.getMemoryPoolMXBeans();
    }
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans(){
        return ManagementFactory.getMemoryManagerMXBeans();
    }
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans(){
        return ManagementFactory.getGarbageCollectorMXBeans();
    }
    public static RuntimeMXBean getRunTimeBeanByFactory() {
        return ManagementFactory.getRuntimeMXBean();
    }

    public static RuntimeMXBean getRunTimeBeanByConnection(MBeanServerConnection conn) {
        return getMXBean(conn, ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
    }
}