package com.guilu.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.Result;
import com.guilu.domain.dto.MenuRequest;
import com.guilu.domain.dto.query.MenuQuery;
import com.guilu.domain.po.SysMenu;
import com.guilu.domain.vo.MenuVO;
import com.guilu.service.AccountService;
import com.guilu.service.ISysMenuService;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜单表 前端控制器
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class SysMenuController {
    private final AccountService accountService;
    private final ISysMenuService menuService;
    @ApiOperation("获取用户菜单")
    @GetMapping("/get/menu/byRole")
    public Result<List<MenuVO>> getByRole(HttpServletRequest request){
        Long roleId = accountService.getRoleId(request);
        return Result.success(menuService.getRoleMenu(roleId));
    }
    @ApiOperation("添加用户菜单")
    @PostMapping("/add/menu")
    public Result<Void> addMenu(@Valid @RequestBody MenuRequest menuRequest, @RequestBody List<Long> roleId){
        menuService.saveMenuAndMap(menuRequest,roleId);
        return Result.success();
    }
    @ApiOperation("更改用户菜单")
    @PutMapping("/update/menu")
    public Result<Void> updateMenu(@RequestParam Long menuId,@Valid @RequestBody MenuRequest menuRequest, @RequestBody List<Long> roleId){
        menuService.updateMenuAndRole(menuId,menuRequest,roleId);
        return Result.success();
    }
    @ApiOperation("删除角色菜单映射")
    @DeleteMapping("/del/menu_role")
    public Result<Void> deleteMenuRole(@RequestParam Long menuId,@RequestBody List<Long> roleId){
        menuService.deleteRoleMap(menuId,roleId);
        return Result.success();
    }
    @ApiOperation("删除菜单")
    @DeleteMapping("/del/menu")
    public Result<Void> delMenu(@RequestBody List<Long> menuId){
        menuService.deleteMenuById(menuId);
        return Result.success();
    }
    @ApiOperation("分页获取菜单数据")
    @GetMapping("/page/query/menu")
    public Result<Page<SysMenu>> pageQueryMenu(@RequestBody MenuQuery menuQuery){
        return Result.success(menuService.pageQueryMenu(menuQuery));
    }
}
