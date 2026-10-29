package com.guilu.properties;

import com.guilu.monitor.sdk.binder.logging.enums.LoggingMetricType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@Data
@ConfigurationProperties("monitor.metrics")
public class MonitorMetricsProperties {
    private boolean enabled = true;
    private boolean jvmEnabled = true;
    private boolean gcEnabled = true;
    private boolean executorEnabled = true;
    private boolean sysDetailInfo = false;
    private Duration executorRefreshInterval = Duration.ofSeconds(10);
    private Duration jvmRefreshInterval = Duration.ofSeconds(10);

    private Logging logging = new Logging();

    @Data
    public static class Logging {
        private boolean enabled = false;
        private LoggingMetricType metricsType = LoggingMetricType.BASICEXPAND;
        private int logCap = 1500;
    }
}