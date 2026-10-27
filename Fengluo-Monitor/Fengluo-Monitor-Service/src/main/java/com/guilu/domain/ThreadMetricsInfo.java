package com.guilu.domain;

import java.lang.management.ThreadInfo;

/**
 * 线程指标信息
 * @param threadInfo 线程信息
 * @param cpuTime 线程累计 CPU 时间
 * @param userTime 线程累计用户态时间
 */
public record ThreadMetricsInfo(ThreadInfo threadInfo, long cpuTime, long userTime) {}
