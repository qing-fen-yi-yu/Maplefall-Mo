package com.guilu.domain.po.ThreadPool;

import lombok.Data;

import java.util.concurrent.TimeUnit;
@Data
public class PoolUpdateConfig {
    private String threadPoolName;
    private Integer coreSize;
    private Integer maxPoolSize;
    private Long keepAlive;
    private TimeUnit unit;
    private Integer queueCapacity;
}
