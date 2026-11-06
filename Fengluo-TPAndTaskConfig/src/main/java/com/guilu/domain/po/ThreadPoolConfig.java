package com.guilu.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName("thread_pool_config")
public class ThreadPoolConfig extends BaseThreadPoolConfig {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属微服务名称
     */
    private String serviceName;

    /**
     * 服务实例配置(Ip:Port)，为空表示服务级配置
     */
    private String instanceConfig;

    /**
     * 线程池实现类型
     */
    private String executorType;

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
