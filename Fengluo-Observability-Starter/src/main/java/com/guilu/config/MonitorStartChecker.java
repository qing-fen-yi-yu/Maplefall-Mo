package com.guilu.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
        import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MonitorStartChecker implements ApplicationRunner {
    private final MeterRegistry registry;

    public MonitorStartChecker(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        registry.getMeters().stream()
                .filter(m -> m.getId().getName().startsWith("process."))
                .forEach(m ->
                        log.info("指标: {} | 类型: {} | 值: {}", m.getId().getName(), m.getClass().getSimpleName(),
                                m instanceof Gauge g ? g.value() : "N/A"));
    }
}
