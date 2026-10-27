package com.guilu.monitor.sdk.spi;

import java.util.Map;

@FunctionalInterface
public interface ThreadPoolSnapshotSource {
    Map<String, ThreadPoolSnapshot> snapshot();
}
