package com.guilu.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.Constants.TPConstant;
import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Manager.updater.TPUpdater;
import com.guilu.domain.dto.TPConfigPageQueryDto;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.domain.po.ThreadPoolConfig;
import com.guilu.domain.query.QueryPage;
import com.guilu.mapper.ThreadPoolConfigMapper;
import com.guilu.service.IThreadPoolConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.service.IThreadPoolConfigSnapshotService;
import com.guilu.utils.UserContext;
import com.guilu.exception.BusinessException.ThreadPoolException;
import com.guilu.enums.RejectHandlerType;
import com.guilu.utils.queryUtil.QueryCreateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * <p>
 * 动态线程池配置表 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@Service
@RequiredArgsConstructor
public class ThreadPoolConfigServiceImpl extends ServiceImpl<ThreadPoolConfigMapper, ThreadPoolConfig> implements IThreadPoolConfigService {
    private final QueryCreateUtils<ThreadPoolConfig> queryCreateUtils = new QueryCreateUtils<>();
    private final IThreadPoolConfigSnapshotService snapshotService;
    private final TPUpdater tpUpdater;
    @Override
    public Page<ThreadPoolConfig> pageQuery(TPConfigPageQueryDto queryDto) {
        QueryPage queryPage = queryDto.getPage();
        Page<ThreadPoolConfig> page = new Page<>(queryPage.getCurrent(), queryPage.getPageSize());
        page = page(page,queryCreateUtils.generateQueryWrapper(queryDto));
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTP(ThreadPoolConfig threadPoolConfig) {
        validateNewConfig(threadPoolConfig);
        LocalDateTime now = LocalDateTime.now();
        threadPoolConfig.setCreatedAt(now);
        threadPoolConfig.setUpdatedAt(now);
        threadPoolConfig.setCreatedBy(UserContext.getUser());
        threadPoolConfig.setUpdatedBy(UserContext.getUser());
        threadPoolConfig.setDeleted(0);
        if (!save(threadPoolConfig)) {
            throw new ThreadPoolException("保存线程池配置失败");
        }
        // 配置与快照在同一事务中保存，更新事件由更新器延迟到提交后发布。
        snapshotService.TPConfigToSna(threadPoolConfig);
        if (Integer.valueOf(1).equals(threadPoolConfig.getEnabled())) {
            updateTPConfig(threadPoolConfig);
        }
    }

    //todo
    @Override
    @Transactional
    public void updateTP(ThreadPoolConfig threadPoolConfig) {
//        updateById(threadPoolConfig);
        //保存当前配置快照
//        snapshotService.TPConfigToSna(threadPoolConfig);
        updateTPConfig(threadPoolConfig);
    }

    private void updateTPConfig(ThreadPoolConfig threadPoolConfig){
        boolean accepted = tpUpdater.publishTPConfigEvent(new TPConfigUpdaterEvent(
                this.getClass().getName()+"-"+ TPConstant.TP_CONFIG_UPD_EVENT,
                PoolUpdateConfig.ofPoolUpdateConfig(threadPoolConfig,
                        threadPoolConfig.getServiceName(),
                        threadPoolConfig.getInstanceConfig())));
        if (!accepted) {
            throw new ThreadPoolException("线程池更新器未接受配置");
        }
    }

    private void validateNewConfig(ThreadPoolConfig config) {
        if (config == null || !StringUtils.hasText(config.getPoolName())
                || !StringUtils.hasText(config.getServiceName()) || !StringUtils.hasText(config.getExecutorType())) {
            throw new ThreadPoolException("线程池名称、服务名称和实现类型不能为空");
        }
        if (config.getCorePoolSize() == null || config.getCorePoolSize() < 0
                || config.getMaxPoolSize() == null || config.getMaxPoolSize() < 1
                || config.getCorePoolSize() > config.getMaxPoolSize()
                || config.getQueueCapacity() == null || config.getQueueCapacity() < 1) {
            throw new ThreadPoolException("线程数或队列容量非法");
        }
        if (config.getKeepAliveSeconds() == null) {
            config.setKeepAliveSeconds(60);
        }
        if (config.getAllowCoreThreadTimeout() == null) {
            config.setAllowCoreThreadTimeout(0);
        }
        if (config.getEnabled() == null) {
            config.setEnabled(1);
        }
        if ((config.getEnabled() != 0 && config.getEnabled() != 1)
                || (config.getAllowCoreThreadTimeout() != 0 && config.getAllowCoreThreadTimeout() != 1)
                || config.getKeepAliveSeconds() < 0
                || (config.getAllowCoreThreadTimeout() == 1 && config.getKeepAliveSeconds() == 0)) {
            throw new ThreadPoolException("启用标志、核心线程超时标志或存活时间非法");
        }
        if (RejectHandlerType.from(config.getRejectedPolicy()) == null) {
            throw new ThreadPoolException("拒绝策略非法: " + config.getRejectedPolicy());
        }
    }
}
