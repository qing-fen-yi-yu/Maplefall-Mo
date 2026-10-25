package com.guilu.domain;

/**
 * 采集时刻的操作系统与 JVM 进程。
 *
 * @param name                   系统名称
 * @param arch                   系统架构
 * @param version                系统版本
 * @param availableProcessors    JVM可用处理器数量
 * @param systemLoadAverage      最近 1 分钟系统平均负载
 * @param systemCpuLoad          CPU使用率
 * @param processCpuLoad         JVM进程cpu使用率
 * @param processCpuTime         JVM进程累计cpu时间单位纳秒
 * @param totalPhysicalMemory    物理内存总量
 * @param freePhysicalMemory     当前空闲物理内存
 * @param committedVirtualMemory 进程可使用的虚拟内存量
 * @param totalSwapSpace         Swap空间总量
 * @param freeSwapSpace          空闲Swap空间
 * @param openFileDescriptorCount 进程当前打开的文件描述符数量（unix）
 * @param maxFileDescriptorCount  进程可打开的文件描述符上限(unix)
 */
public record SystemMetricsInfo(String name, String arch, String version, int availableProcessors,
        double systemLoadAverage, double systemCpuLoad, double processCpuLoad, long processCpuTime,
        long totalPhysicalMemory, long freePhysicalMemory, long committedVirtualMemory,
        long totalSwapSpace, long freeSwapSpace, long openFileDescriptorCount, long maxFileDescriptorCount) {}
