package com.guilu.config;

import com.guilu.monitor.sdk.binder.ThreadPoolMetricsBinder;
import com.guilu.monitor.sdk.spi.ThreadPoolSnapshotSource;
import com.guilu.properties.MonitorMetricsProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = MonitorMetricsAutoConfiguration.class)
@ConditionalOnClass({MeterRegistry.class, ThreadPoolMetricsBinder.class})
@ConditionalOnBean(ThreadPoolSnapshotSource.class)
@ConditionalOnProperty(prefix = "monitor.metrics",
        name = {"enabled", "executor-enabled"}, havingValue = "true", matchIfMissing = true)
public class ThreadPoolMetricsAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public ThreadPoolMetricsBinder fengluoExecutorMetrics(ThreadPoolSnapshotSource source,
                                                          MonitorMetricsProperties properties) {
        return new ThreadPoolMetricsBinder(source, properties.getExecutorRefreshInterval());
    }
}
