package com.guilu.domain.po.ThreadPool;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 线程池配置、配置快照和更新消息共用的参数。存活时间统一使用秒。
 */
@Data
@Accessors(chain = true)
public abstract class BaseThreadPoolConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 线程池名称；快照表通过 configId 关联名称，不持久化此字段。 */
    private String poolName;

    /** 核心线程数。 */
    private Integer corePoolSize;

    /** 最大线程数。 */
    private Integer maxPoolSize;

    /** 任务队列容量。 */
    private Integer queueCapacity;

    /** 非核心线程空闲存活时间，单位秒；null 表示更新时保持不变。 */
    private Integer keepAliveSeconds;

    /** 是否允许核心线程超时：0 否，1 是。 */
    private Integer allowCoreThreadTimeout;

    /** 任务拒绝策略。 */
    private String rejectedPolicy;
}
