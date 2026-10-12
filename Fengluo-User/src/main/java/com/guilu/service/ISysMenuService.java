package com.guilu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.dto.MenuRequest;
import com.guilu.domain.dto.query.MenuQuery;
import com.guilu.domain.po.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guilu.domain.vo.MenuVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * <p>
 * 菜单表 服务类
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
public interface ISysMenuService extends IService<SysMenu> {

    List<MenuVO> getRoleMenu(Long roleId);

    void saveMenuAndMap(MenuRequest menuRequest, List<Long> roleId);

    void updateMenuAndRole(Long menuId,@Valid MenuRequest menuRequest, List<Long> roleId);

    void deleteRoleMap(Long menuId, List<Long> roleId);

    void deleteMenuById(List<Long> menuId);

    Page<SysMenu> pageQueryMenu(MenuQuery menuQuery);
}
