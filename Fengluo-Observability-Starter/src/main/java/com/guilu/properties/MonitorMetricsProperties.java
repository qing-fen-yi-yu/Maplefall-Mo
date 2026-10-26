package com.guilu.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties("monitor.metrics")
public class MonitorMetricsProperties {
    private boolean enabled = true; // 开启所有的指标指标采集，如果此处为false则下面的其它指标都不讲进行注册
    private boolean jvmEnabled = true; // jvm指标采集
    private boolean gcEnabled = true; //Gc指标采集
    private boolean executorEnabled = true; //线程池指标采集
    private boolean sysDetailInfo = false; //系统详细信息采集，默认不采集，使用简略信息
    private Duration executorRefreshInterval = Duration.ofSeconds(10);
    private Duration jvmRefreshInterval = Duration.ofSeconds(10); // 系统/死锁/明细数据的缓存有效期
}
