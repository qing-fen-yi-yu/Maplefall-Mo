package com.guilu.controller.TP;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.domain.Result;
import com.guilu.domain.dto.TPConfigPageQueryDto;
import com.guilu.domain.po.ThreadPoolConfig;
import com.guilu.service.IThreadPoolConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 动态线程池配置表 前端控制器
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@RestController
@RequestMapping("/threadPoolConfigAndSnapshot")
@RequiredArgsConstructor
public class ThreadPoolConfigController {
    private final IThreadPoolConfigService tpConfigService;

    @ApiOperation("根据ID查询持久化线程池配置")
    @GetMapping("/get/storage/{id}")
    public Result<ThreadPoolConfig> getByIdStorage(@PathVariable Long id) {
        return  Result.success(tpConfigService.getById(id));
    }
    @ApiOperation("分页查询完成持久化的线程池配置")
    @GetMapping("/get/storage/page")
    public Result<Page<ThreadPoolConfig>> pageQueryTpConfig(@RequestBody TPConfigPageQueryDto queryDto) {
        return Result.success(tpConfigService.pageQuery(queryDto));
    }
    @ApiOperation("添加线程池配置")
    @PostMapping("/addTP")
    public Result<Void> addTP(@RequestBody ThreadPoolConfig threadPoolConfig) {
        tpConfigService.addTP(threadPoolConfig);
        return Result.success();
    }
}
