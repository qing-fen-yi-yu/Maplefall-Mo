package com.guilu.controller.TP;


import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.domain.Result;
import com.guilu.domain.dto.TPConfigSnapshotQueryDto;
import com.guilu.domain.po.ThreadPoolConfigSnapshot;
import com.guilu.service.IThreadPoolConfigSnapshotService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @ApiOperation("查询某个服务的当前线程池配置")
    @GetMapping("/get/byService")
    public Result<Set<DynamicPoolSnapshot>> pageQueryAll(@RequestBody TPConfigSnapshotQueryDto tpConfigSnapshot) {
        return Result.success(tpConfigSnapshotService.getConfigSnaByServiceName(tpConfigSnapshot));
    }
    @ApiOperation("根据线程池id删除所有快照")
    @DeleteMapping("/delete/{poolConfigId}")
    public Result<Void> deleteByPoolId(@PathVariable Long poolConfigId) {
        tpConfigSnapshotService.deleteByConfigId(poolConfigId);
        return Result.success();
    }
    @ApiOperation("根据Id删除快照")
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        tpConfigSnapshotService.removeById(id);
        return Result.success();
    }

    @ApiOperation("根据线程池Id查询所有快照")
    @GetMapping("/get/byPoolId/{poolConfigId}")
    public Result<Set<ThreadPoolConfigSnapshot>> pageQueryAll(@PathVariable Long poolConfigId) {
        return Result.success(tpConfigSnapshotService.queryByConfigId(poolConfigId));
    }
    @ApiOperation("根据Id查询快照")
    @GetMapping("/get/{id}")
    public Result<ThreadPoolConfigSnapshot> pageQueryById(@PathVariable Long id) {
        return Result.success(tpConfigSnapshotService.getById(id));
    }
}
