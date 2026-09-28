package com.guilu.controller;


import com.guilu.threadPool.annotation.DynamicThreadPool;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>
 * 用户-角色关系 前端控制器
 * </p>
 *
 * @author 归鹭
 * @since 2026-09-25
 */
@RestController
@RequestMapping("/role")
public class SysUserRoleController {
    @DynamicThreadPool
    private final ExecutorService executorService =  Executors.newFixedThreadPool(4);
}
