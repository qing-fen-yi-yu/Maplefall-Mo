package com.guilu.mapper;

import com.guilu.domain.dto.PrivilegePathRow;
import com.guilu.domain.po.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 菜单表 Mapper 接口
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询"路径 - 角色"关系，供权限缓存发布使用。
     * 只取启用状态且未逻辑删除的菜单与角色，一行代表一条 (路径, 角色) 授权。
     */
    List<PrivilegePathRow> selectPrivileges();
}
