package com.guilu.monitor.sdk.binder.logging;

import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.List;

public abstract class AbstractLoggingBinder implements MeterBinder {
    static final String EVENTS = "log4j2.events";
    static final List<String> LEVELS =
            List.of("trace", "debug", "info", "warn", "error", "fatal");
}
