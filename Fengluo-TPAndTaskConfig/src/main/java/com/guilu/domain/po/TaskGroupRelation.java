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
 * 任务组-任务关联表
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("task_group_relation")
public class TaskGroupRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务组ID
     */
    @TableField("group_id")
    private Long groupId;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 组内执行顺序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 前置依赖任务ID，为空表示无依赖
     */
    @TableField("dependency_task_id")
    private Long dependencyTaskId;

    /**
     * 失败后是否继续：0否，1是
     */
    @TableField("continue_on_failure")
    private Integer continueOnFailure;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;


}
