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
 * 任务组配置表
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("task_group")
public class TaskGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务组名称
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 任务组描述
     */
    @TableField("description")
    private String description;

    /**
     * 任务组状态：0禁用，1启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 执行模式：SEQUENTIAL串行，PARALLEL并行
     */
    @TableField("execution_mode")
    private String executionMode;

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
