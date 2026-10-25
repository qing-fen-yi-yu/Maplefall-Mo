package com.guilu.Manager.reporter;

import com.guilu.Manager.PoolManager;
import com.guilu.domain.DynamicPoolSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@Slf4j
public class PoolReporter extends PoolManager{

    private final String applicationName;

    public PoolReporter(Environment env) {
        this.applicationName = env.getProperty("spring.application.name", "unknown");
    }

    public Map<String, DynamicPoolSnapshot> snapshot() {
        Map<String, DynamicPoolSnapshot> result = new HashMap<>();
        getAlivePool().forEach((name, executor) -> {
            try {
                DynamicPoolSnapshot snapshot = buildSnapshot(name, executor);
                if (snapshot != null) {
                    result.put(name, snapshot);
                }
            } catch (Exception e) {
                log.warn("采集线程池快照失败: name={}, type={}", name,
                        executor == null ? "null" : executor.getClass().getName(), e);
            }
        });
        return Map.copyOf(result);
    }

    private DynamicPoolSnapshot buildSnapshot(String registryName, Executor executor) {
        return DynamicPoolSnapshot.collectPoolData(registryName, executor);
    }
}
