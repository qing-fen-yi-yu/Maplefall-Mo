package com.guilu.domain.dto;

import com.guilu.enums.MenuType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuRequest {
    @NotNull(message = "值禁止为空")
    private MenuType type;
    @NotNull(message = "值禁止为空")
    private Long parentId;
    /**
     * 菜单或权限名称
     */
    @NotNull(message = "值禁止为空")
    private String menuName;

    /**
     * 前端路由路径或资源路径
     */
    @NotNull
    private String path;

    /**
     * 前端页面组件路径
     */
    private String component;

    /**
     * 后端接口权限标识或权限表达式
     */
    private String permission;

    /**
     * 菜单图标标识
     */
    private String icon;

    /**
     * 同级菜单或资源的排序号
     */
    private Integer sort;

    /**
     * 前端是否显示：0否，1是
     */
    private Integer visible;

    /**
     * 资源状态：0禁用，1正常
     */
    private Integer status;
}
