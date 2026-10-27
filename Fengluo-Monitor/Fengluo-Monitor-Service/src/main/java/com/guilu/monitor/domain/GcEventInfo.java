package com.guilu.monitor.domain;

import java.lang.management.MemoryUsage;
import java.util.Map;

/**
 * Gc事件器
 */
public record GcEventInfo(String collector, String action, String cause, long id,
        long startTime, long endTime, long duration,
        Map<String, MemoryUsage> memoryBeforeGc, Map<String, MemoryUsage> memoryAfterGc) {
    public GcEventInfo {
        memoryBeforeGc = Map.copyOf(memoryBeforeGc);
        memoryAfterGc = Map.copyOf(memoryAfterGc);
    }
}
