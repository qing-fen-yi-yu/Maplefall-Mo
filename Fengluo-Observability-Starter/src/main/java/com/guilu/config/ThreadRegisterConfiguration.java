package com.guilu.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/** Compatibility entry point. Thread-pool registration is owned by DynamicTP. */
@Deprecated
@AutoConfiguration(after = DynamicPoolAutoConfiguration.class)
@Import(DynamicPoolAutoConfiguration.class)
public class ThreadRegisterConfiguration {
}