package com.guilu.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 线程池配置快照表
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("thread_pool_config_snapshot")
public class ThreadPoolConfigSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 线程池配置ID
     */
    private Long configId;

    /**
     * 线程池配置版本号
     */
    private Integer version;

    /**
     * 核心线程数
     */
    private Integer corePoolSize;

    /**
     * 最大线程数
     */
    private Integer maxPoolSize;

    /**
     * 任务队列容量
     */
    private Integer queueCapacity;

    /**
     * 非核心线程空闲存活时间，单位秒
     */
    private Integer keepAliveSeconds;

    /**
     * 是否允许核心线程超时：0否，1是
     */
    private Integer allowCoreThreadTimeout;

    /**
     * 任务拒绝策略
     */
    private String rejectedPolicy;

    /**
     * 配置变更类型
     */
    private String changeType;

    /**
     * 执行更改的用户ID
     */
    private Long changedBy;

    /**
     * 配置变更时间
     */
    private LocalDateTime changedAt;

    /**
     * 线程池配置快照JSON
     */
    private String configJson;


}
