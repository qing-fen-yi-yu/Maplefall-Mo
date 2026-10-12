package com.guilu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.constants.ResultInfo;
import com.guilu.domain.dto.MenuRequest;
import com.guilu.domain.dto.query.MenuQuery;
import com.guilu.domain.po.SysMenu;
import com.guilu.domain.po.SysRole;
import com.guilu.domain.po.SysRoleMenu;
import com.guilu.domain.query.QueryPage;
import com.guilu.domain.vo.MenuVO;
import com.guilu.exception.BusinessException.BadDataException;
import com.guilu.exception.BusinessException.CommonException;
import com.guilu.exception.BusinessException.SQLOptionalException;
import com.guilu.mapper.SysMenuMapper;
import com.guilu.mapper.SysRoleMapper;
import com.guilu.mapper.SysRoleMenuMapper;
import com.guilu.mapper.SysUserRoleMapper;
import com.guilu.service.ISysMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.utils.BeanUtils;
import com.guilu.utils.CollUtils;
import com.guilu.utils.ObjectUtils;
import com.guilu.utils.queryUtil.QueryCreateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {
    private final SysUserRoleMapper urMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper rmMapper;
    private final QueryCreateUtils<SysRoleMenu> RMqueryCreateUtils = new QueryCreateUtils<>();
    private final QueryCreateUtils<SysMenu> menuQueryCreateUtils = new QueryCreateUtils<>();
    @Cacheable(value = "menuTree",key = "#rId")
    @Override
    public List<MenuVO> getRoleMenu(Long rId) {
        Long roleId = urMapper.getRoleById(rId);
        SysRole role = roleMapper.selectById(roleId);
        if(role == null){
            throw new BadDataException(ResultInfo.Msg.INVALID_USER_TYPE);
        }
        List<Long> menuIds = rmMapper.selectMenuIdsByRoleId(roleId);
        List<SysMenu> sysMenus = baseMapper.selectByIds(menuIds);
        return createMenuTree(BeanUtils.copyList(sysMenus,MenuVO.class),0L);
    }

    @Override
    @Transactional
    public void saveMenuAndMap(MenuRequest menuRequest, List<Long> roleId) {
        SysMenu menu = BeanUtils.copyBean(menuRequest, SysMenu.class);
        Long parentId = menu.getParentId();
        if(!(parentId == null || parentId == 0L)){
            if(ObjectUtils.isEmpty(getById(parentId))){
                throw new BadDataException(ResultInfo.Msg.REQUEST_PARAM_ILLEGAL);
            }
        }
        save(menu);
        rmMapper.saveRoleMenuAll(menu.getId(),roleId);
    }

    @Override
    public void updateMenuAndRole(Long menuId,MenuRequest menuRequest, List<Long> roleId) {
        SysMenu menu = BeanUtils.copyBean(menuRequest, SysMenu.class);
        menu.setId(menuId);
        updateById(menu);
        if(roleId != null){
            //执行映射新增操作
            rmMapper.saveRoleMenuAll(menuId,roleId);
        }
    }

    @Override
    public void deleteRoleMap(Long menuId, List<Long> roleId) {
        rmMapper.deleteRoleAndMenu(menuId,roleId);
    }

    @Transactional
    @Override
    public void deleteMenuById(List<Long> menuId) {
        if(!removeByIds(menuId) && rmMapper.deleteMenuIds(menuId)){
            throw new SQLOptionalException(ResultInfo.Msg.DB_DELETE_EXCEPTION);
        }
    }

    @Override
    public Page<SysMenu> pageQueryMenu(MenuQuery menuQuery) {
        QueryWrapper<SysMenu> sysRoleMenuQueryWrapper = menuQueryCreateUtils.generateQueryWrapper(menuQuery);
        QueryPage PVal = menuQuery.getPage();
        return page(new Page<>(PVal.getCurrent(), PVal.getPageSize()),sysRoleMenuQueryWrapper);
    }

    private List<MenuVO> createMenuTree(List<MenuVO> menus, Long rootParentId) {
        if (CollUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }
        //记忆化节点初始化
        Map<Long, MenuVO> nodeMap = new HashMap<>(menus.size() * 4 / 3 + 1);
        for (MenuVO vo : menus) {
            vo.setChildren(new ArrayList<>());
            nodeMap.put(vo.getId(), vo);
        }

        List<MenuVO> roots = new ArrayList<>();
        for (MenuVO vo : menus) {
            Long pid = vo.getParentId();
            if (pid == null || pid.equals(rootParentId)) {
                roots.add(vo);
                continue;
            }
            MenuVO parent = nodeMap.get(pid);
            //只有存在的父节点才会存入，其他直接丢弃，避免数据异常
            if (parent != null) {
                parent.getChildren().add(vo);
            }
        }
        // 3. 排序
        //3.1 父节点排序
        Comparator<MenuVO> cmp = Comparator
                .comparing(MenuVO::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(MenuVO::getId);
        roots.sort(cmp);
        //3.2 子节点排序
        nodeMap.values().forEach(v -> v.getChildren().sort(cmp));

        return roots;
    }
}
