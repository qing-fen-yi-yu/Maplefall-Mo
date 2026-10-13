package com.guilu.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * 任务定义
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("task_definition")
public class TaskDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;

    /**
     * 任务类型
     */
    @TableField("task_type")
    private String taskType;

    /**
     * 任务处理器标识
     */
    @TableField("handler")
    private String handler;

    /**
     * Cron表达式
     */
    @TableField("cron_expression")
    private String cronExpression;

    /**
     * 任务状态：0禁用，1启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 优先级，数值越大优先级越高
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 超时时间，单位秒
     */
    @TableField("timeout_seconds")
    private Integer timeoutSeconds;

    /**
     * 最大重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 重试间隔，单位秒
     */
    @TableField("retry_interval_seconds")
    private Integer retryIntervalSeconds;

    /**
     * 并发策略
     */
    @TableField("concurrent_policy")
    private String concurrentPolicy;

    /**
     * 错过触发策略
     */
    @TableField("misfire_policy")
    private String misfirePolicy;

    /**
     * 任务参数
     */
    @TableField("params_json")
    private String paramsJson;

    /**
     * 任务描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 0未删除，1已删除
     */
    @TableField("deleted")
    private Integer deleted;


}
