package com.guilu.controller.TP;


import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.domain.Result;
import com.guilu.service.IThreadPoolConfigSnapshotService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 线程池配置快照表 前端控制器
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@RestController
@RequestMapping("/threadPoolConfigSnapshot")
@RequiredArgsConstructor
public class ThreadPoolConfigSnapshotController {
    private final IThreadPoolConfigSnapshotService tpConfigSnapshotService;
    @ApiOperation("查询所有线程池配置")
    @GetMapping("/get/all")
    public Result<Map<String, Set<DynamicPoolSnapshot>>> pageQueryAll() {
        return Result.success(tpConfigSnapshotService.queryAll());
    }
}
