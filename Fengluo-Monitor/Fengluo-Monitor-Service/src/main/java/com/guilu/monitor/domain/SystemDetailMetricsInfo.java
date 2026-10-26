package com.guilu.monitor.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SystemDetailMetricsInfo {
    private SystemMetricsInfo systemMetricsInfo;
    private Integer cpuCount; // cpu核数
    private Long cpuWaitTime;
    private Double cpuWait; // cpu等待率
    private Double cpuSysUsed; //cpu系统占用率
    private Double cpuIdle; //cpu空闲率
    private Double cpuUserUsed; //cpu用户占用率
    private Double cpuTotalUsed; // cpu总使用率
    private Long memUsed; //内存已使用大小
    private Double memRate; // 内存占用率
    private Long jvmTotal; // jvm总使用大小
    private Long jvmFree; //jvm 空闲大小
}
