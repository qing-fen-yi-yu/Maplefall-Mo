package com.guilu.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.guilu.enums.MenuType;
import lombok.Data;

import java.util.List;

@Data
public class MenuVO {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long parentId;
    //子菜单
    private List<MenuVO> children;

    /**
     * 目录、菜单、按钮
     */
    private MenuType type;

    /**
     * 菜单或权限名称
     */
    private String menuName;

    /**
     * 前端路由路径或资源路径
     */
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
}
