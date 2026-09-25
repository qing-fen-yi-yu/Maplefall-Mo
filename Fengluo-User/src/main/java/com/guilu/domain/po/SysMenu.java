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
 * 菜单表
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_menu")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父级菜单或权限ID，0表示根节点
     */
    private Long parentId;

    /**
     * 目录、菜单、按钮
     */
    private String type;

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

    /**
     * 资源状态：0禁用，1正常
     */
    private Integer status;

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
