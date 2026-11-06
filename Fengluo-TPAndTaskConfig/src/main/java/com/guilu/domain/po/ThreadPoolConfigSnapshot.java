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
 * 线程池配置快照表
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@TableName(value = "thread_pool_config_snapshot", excludeProperty = "poolName")
public class ThreadPoolConfigSnapshot extends BaseThreadPoolConfig {

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
