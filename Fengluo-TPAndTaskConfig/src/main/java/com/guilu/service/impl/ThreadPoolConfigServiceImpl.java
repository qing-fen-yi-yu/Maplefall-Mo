package com.guilu.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.Constants.TPConstant;
import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Event.TPCreateEvent;
import com.guilu.Manager.updater.TPUpdater;
import com.guilu.client.TPUpdaterClient;
import com.guilu.domain.dto.TPConfigPageQueryDto;
import com.guilu.domain.po.ThreadPool.PoolCreateConfig;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.domain.po.ThreadPoolConfig;
import com.guilu.domain.query.QueryPage;
import com.guilu.enums.TPType;
import com.guilu.mapper.ThreadPoolConfigMapper;
import com.guilu.service.IThreadPoolConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.service.IThreadPoolConfigSnapshotService;
import com.guilu.utils.UserContext;
import com.guilu.exception.BusinessException.ThreadPoolException;
import com.guilu.enums.RejectHandlerType;
import com.guilu.utils.queryUtil.QueryCreateUtils;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

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
    @Resource(type = TPUpdaterClient.class)
    private  TPUpdater tpUpdater;
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

    @Override
    @Transactional
    public void updateTP(ThreadPoolConfig threadPoolConfig) {
        boolean b = updateById(threadPoolConfig);
        if(b){
//            保存成功则当前配置快照
        snapshotService.TPConfigToSna(threadPoolConfig);
        }
        updateTPConfig(threadPoolConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIdWithSnapshot(Long id, Boolean withSnapshot) throws ExecutionException, InterruptedException {
        ThreadPoolConfig threadPoolConfig = getById(id);
        boolean b = removeById(id);
        if(withSnapshot){
            //删除成功才进行删除快照
            if(b){
                snapshotService.deleteByConfigId(id);
                tpUpdater.removeDynamicTP(threadPoolConfig.getPoolName(),threadPoolConfig.getServiceName(),threadPoolConfig.getInstanceConfig());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) throws ExecutionException, InterruptedException {
        boolean update = lambdaUpdate().eq(ThreadPoolConfig::getEnabled, 0)
                .set(ThreadPoolConfig::getEnabled, 1)
                .eq(ThreadPoolConfig::getId, id).update();
        if(update){
            ThreadPoolConfig threadPoolConfig = getById(id);
            TPType tpType = TPType.of(threadPoolConfig.getExecutorType());
            tpUpdater.addTPConfig(new TPCreateEvent(this,
                    PoolCreateConfig.of(threadPoolConfig,tpType,threadPoolConfig.getServiceName(),threadPoolConfig.getInstanceConfig())));
        }
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
