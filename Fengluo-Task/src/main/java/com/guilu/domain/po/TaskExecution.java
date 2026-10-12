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
 * 任务执行记录
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("task_execution")
public class TaskExecution implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 执行ID，全局唯一
     */
    @TableField("execution_id")
    private String executionId;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 任务组ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 服务名
     */
    @TableField("service_name")
    private String serviceName;

    /**
     * 实例ID
     */
    @TableField("instance_id")
    private String instanceId;

    /**
     * 触发类型：CRON/MANUAL/API/RETRY/SYSTEM
     */
    @TableField("trigger_type")
    private String triggerType;

    /**
     * 计划触发时间
     */
    @TableField("scheduled_at")
    private LocalDateTime scheduledAt;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 执行耗时，单位毫秒
     */
    @TableField("duration_ms")
    private Long durationMs;

    /**
     * 执行状态：0待执行，1执行中，2成功，3失败，4已取消，5超时
     */
    @TableField("status")
    private Integer status;

    /**
     * 当前重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 执行工作节点
     */
    @TableField("worker")
    private String worker;

    /**
     * 请求ID
     */
    @TableField("request_id")
    private String requestId;

    /**
     * 链路追踪ID
     */
    @TableField("trace_id")
    private String traceId;

    /**
     * 错误码
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 结果摘要
     */
    @TableField("result_summary")
    private String resultSummary;

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


}
