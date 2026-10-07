package com.guilu.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.Result;
import com.guilu.domain.dto.RoleMenuRequest;
import com.guilu.domain.dto.query.RoleQuery;
import com.guilu.domain.dto.update.RoleUpdateRequest;
import com.guilu.domain.dto.update.StatusUpdateRequest;
import com.guilu.domain.po.SysRole;
import com.guilu.service.ISysRoleService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 角色表 前端控制器
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class SysRoleController {
    private final ISysRoleService roleService;

    @ApiOperation("新增角色")
    @PostMapping("/add")
    public Result<Void> addRole(@RequestBody SysRole sysRole) {
        roleService.insertRole(sysRole);
        roleService.publishPrivileges();
        return Result.success();
    }

    @ApiOperation("角色分页查询")
    @PostMapping("/page/query")
    public Result<Page<SysRole>> pageQuery(@RequestBody RoleQuery roleQuery) {
        return Result.success(roleService.pageQuery(roleQuery));
    }

    @ApiOperation("角色详情")
    @GetMapping("/detail/{id}")
    public Result<SysRole> detail(@PathVariable Long id) {
        return Result.success(roleService.getRoleDetail(id));
    }

    @ApiOperation("修改角色")
    @PutMapping("/update")
    public Result<Void> updateRole(@RequestBody RoleUpdateRequest roleUpdateRequest) {
        roleService.updateRole(roleUpdateRequest);
        roleService.publishPrivileges();
        return Result.success();
    }

    @ApiOperation("启用/停用角色")
    @PutMapping("/update/status")
    public Result<Void> updateStatus(@RequestBody StatusUpdateRequest statusUpdateRequest) {
        roleService.updateStatus(statusUpdateRequest.getId(), statusUpdateRequest.getStatus());
        roleService.publishPrivileges();
        return Result.success();
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> removeRole(@PathVariable Long id) {
        roleService.removeRole(id);
        roleService.publishPrivileges();
        return Result.success();
    }

    @ApiOperation("查询角色已绑定的菜单/权限ID")
    @GetMapping("/menu/{roleId}")
    public Result<List<Long>> listMenuIds(@PathVariable Long roleId) {
        return Result.success(roleService.listMenuIds(roleId));
    }

    @ApiOperation("保存角色授权（全量覆盖，空集合表示清空）")
    @PutMapping("/menu")
    public Result<Void> replaceMenus(@RequestBody RoleMenuRequest roleMenuRequest) {
        roleService.replaceMenus(roleMenuRequest);
        roleService.publishPrivileges();
        return Result.success();
    }

    @ApiOperation("清空角色授权")
    @DeleteMapping("/menu/{roleId}")
    public Result<Void> clearMenus(@PathVariable Long roleId) {
        roleService.clearMenus(roleId);
        roleService.publishPrivileges();
        return Result.success();
    }

    @ApiOperation("手动重新发布权限缓存")
    @PostMapping("/privilege/republish")
    public Result<Void> republishPrivileges() {
        roleService.publishPrivileges();
        return Result.success();
    }
}
