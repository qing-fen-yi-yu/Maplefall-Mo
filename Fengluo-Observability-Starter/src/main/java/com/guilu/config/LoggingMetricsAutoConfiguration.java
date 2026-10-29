package com.guilu.config;

import com.guilu.monitor.sdk.binder.logging.ExpandAppender;
import com.guilu.monitor.sdk.binder.logging.enums.LoggingMetricType;
import com.guilu.monitor.sdk.binder.logging.AbstractLoggingBinder;
import com.guilu.monitor.sdk.binder.logging.BasicExpandMetricsBinder;
import com.guilu.properties.MonitorMetricsProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

//日志配置器
@AutoConfiguration
@EnableConfigurationProperties(MonitorMetricsProperties.class)
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "monitor.metrics.logging", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class LoggingMetricsAutoConfiguration implements Ordered {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(AbstractLoggingBinder.class)
    public AbstractLoggingBinder loggingBinder(MonitorMetricsProperties props) {
        LoggingMetricType type = props.getLogging().getMetricsType();
        AbstractLoggingBinder binder = null;
        binder = new BasicExpandMetricsBinder();
        return binder;
    }

    @Bean
    public ExpandAppender expandAppender(MonitorMetricsProperties props) {
        return new ExpandAppender(props.getLogging().getLogCap());
    }
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
