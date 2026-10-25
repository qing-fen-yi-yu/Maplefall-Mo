package com.guilu.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties("monitor.metrics")
public class MonitorMetricsProperties {
    private boolean enabled = true;
    private boolean jvmEnabled = true;
    private boolean gcEnabled = true;
    private boolean executorEnabled = true;
    private Duration executorRefreshInterval = Duration.ofSeconds(10);
}
