package com.guilu.service.impl;

import com.guilu.domain.po.SysUserRole;
import com.guilu.mapper.SysUserRoleMapper;
import com.guilu.service.ISysUserRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户-角色关系 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements ISysUserRoleService {

    @Override
    public Long selectPrimaryRoleId(Long id) {
        return getBaseMapper().selectPrimaryRoleId(id);
    }
}
