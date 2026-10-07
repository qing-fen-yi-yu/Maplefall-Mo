package com.guilu.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.Result;
import com.guilu.domain.dto.UserRoleRequest;
import com.guilu.domain.dto.query.UserRoleQuery;
import com.guilu.domain.po.SysUserRole;
import com.guilu.service.ISysUserRoleService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 用户-角色关系 前端控制器
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@RestController
@RequestMapping("/userRole")
@RequiredArgsConstructor
public class SysUserRoleController {
    private final  ISysUserRoleService sysUserRoleService;

    @ApiOperation("用户-角色映射分页查询")
    @PostMapping("/page/query")
    public Result<Page<SysUserRole>> pageQueryUserRole(@RequestBody UserRoleQuery userRoleQuery){
        return Result.success(sysUserRoleService.pageQuery(userRoleQuery));
    }

    @ApiOperation("查询用户当前生效的角色ID")
    @GetMapping("/query/byUserId")
    public Result<Long> selectPrimaryRoleId(@RequestParam Long userId){
        return Result.success(sysUserRoleService.selectPrimaryRoleId(userId));
    }

    @ApiOperation("添加用户-角色映射表")
    @PostMapping("/add/userRole")
    public Result<Void> addUserRole(@RequestBody UserRoleRequest userRoleRequest){
        sysUserRoleService.addUserRole(userRoleRequest);
        return Result.success();
    }

    @ApiOperation("批量设置用户角色")
    @PostMapping("/batch")
    public Result<Void> addUserRoleBatch(@RequestBody List<UserRoleRequest> requests){
        sysUserRoleService.assignRoleBatch(requests);
        return Result.success();
    }

    @ApiOperation("删除角色的身份映射-降级为普通用户")
    @DeleteMapping("/del/{userId}")
    public  Result<Void> deleteUserRole(@PathVariable Long userId){
        sysUserRoleService.removeByUserId(userId);
        return Result.success();
    }

    @ApiOperation("删除user_role映射")
    @DeleteMapping("/del")
    public Result<Void> deleteById(@RequestParam Long id){
        sysUserRoleService.removeById(id);
        return Result.success();
    }

    @ApiOperation("更新用户-角色映射权限")
    @PutMapping("/update/userRole")
    public Result<Void> updateUserRole(@RequestBody UserRoleRequest userRoleRequest){
        sysUserRoleService.updateUserRole(userRoleRequest);
        return  Result.success();
    }

}
