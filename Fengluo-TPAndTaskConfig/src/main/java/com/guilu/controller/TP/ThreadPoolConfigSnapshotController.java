package com.guilu.controller.TP;


import com.guilu.service.IThreadPoolConfigSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

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
}
