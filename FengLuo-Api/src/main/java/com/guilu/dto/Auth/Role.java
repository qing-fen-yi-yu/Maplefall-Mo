package com.guilu.dto.Auth;

import java.time.LocalDateTime;

public class Role {
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
}
