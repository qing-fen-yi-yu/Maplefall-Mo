package com.guilu.domain;

import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.List;

public record MemoryPoolInfo(String name, MemoryType type, MemoryUsage usage,
        MemoryUsage peakUsage, MemoryUsage collectionUsage, List<String> managerNames,
        ThresholdInfo usageThreshold, ThresholdInfo collectionUsageThreshold) {
    public MemoryPoolInfo {
        managerNames = List.copyOf(managerNames);
    }
    public MemoryPoolInfo(String name, MemoryType type, MemoryUsage usage,
            MemoryUsage peakUsage, MemoryUsage collectionUsage) {
        this(name, type, usage, peakUsage, collectionUsage, List.of(), null, null);
    }
    public record ThresholdInfo(boolean supported, long value, long count, Boolean exceeded) {
        public static ThresholdInfo unsupported() {
            return new ThresholdInfo(false, -1L, -1L, null);
        }
    }
}
