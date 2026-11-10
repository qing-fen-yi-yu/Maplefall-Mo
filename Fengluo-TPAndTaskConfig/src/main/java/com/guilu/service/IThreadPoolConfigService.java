package com.guilu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.domain.dto.TPConfigPageQueryDto;
import com.guilu.domain.po.ThreadPoolConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 动态线程池配置表 服务类
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
public interface IThreadPoolConfigService extends IService<ThreadPoolConfig> {

    Page<ThreadPoolConfig> pageQuery(TPConfigPageQueryDto queryDto);

    void addTP(ThreadPoolConfig threadPoolConfig);

    void updateTP(ThreadPoolConfig threadPoolConfig);

    void deleteByIdWithSnapshot(Long id, Boolean withSnapshot);

    void enable(Long id);
}
