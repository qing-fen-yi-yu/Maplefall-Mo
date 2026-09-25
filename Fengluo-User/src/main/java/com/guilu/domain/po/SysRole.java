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
 * 角色表
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_role")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码，系统内唯一
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色介绍
     */
    private String description;

    /**
     * 角色状态：0禁用，1正常
     */
    private Integer status;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后修改时间
     */
    private LocalDateTime updatedAt;

    /**
     * 0未删除，1已删除
     */
    private Integer deleted;

    /**
     * 乐观锁版本号
     */
    private Integer version;


}
