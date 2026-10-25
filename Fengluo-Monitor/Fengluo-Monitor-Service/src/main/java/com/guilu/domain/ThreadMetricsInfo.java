package com.guilu.domain;
import java.lang.management.ThreadInfo;

/**
 * 线程指标信息
 * @param threadInfo
 * @param cpuTime
 * @param userTime
 */
public record ThreadMetricsInfo(ThreadInfo threadInfo, long cpuTime, long userTime) {}
