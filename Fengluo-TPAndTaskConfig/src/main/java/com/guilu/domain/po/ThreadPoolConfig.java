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
 * 动态线程池配置表
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("thread_pool_config")
public class ThreadPoolConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 线程池名称
     */
    private String poolName;

    /**
     * 所属微服务名称
     */
    private String serviceName;

    /**
     * 服务实例ID，为空表示服务级配置
     */
    private String instanceId;

    /**
     * 线程池实现类型
     */
    private String executorType;

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
     * 是否启用该线程池配置：0否，1是
     */
    private Integer enabled;

    /**
     * 线程池配置说明
     */
    private String description;

    /**
     * 创建人用户ID
     */
    private Long createdBy;

    /**
     * 更新人用户ID
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 0未删除，1已删除
     */
    private Integer deleted;


}
