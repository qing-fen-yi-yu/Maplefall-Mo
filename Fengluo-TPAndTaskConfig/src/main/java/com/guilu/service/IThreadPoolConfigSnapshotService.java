package com.guilu.service;

import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.domain.po.ThreadPoolConfig;
import com.guilu.domain.po.ThreadPoolConfigSnapshot;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 线程池配置快照表 服务类
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
public interface IThreadPoolConfigSnapshotService extends IService<ThreadPoolConfigSnapshot> {

    Map<String, Set<DynamicPoolSnapshot>> queryAll();

    void TPConfigToSna(ThreadPoolConfig threadPoolConfig);
}
