package com.guilu.DynamicTPConfig;

import com.guilu.Event.TPConfigUpdaterEvent;
import com.guilu.Manager.updater.TPUpdater;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;
import com.guilu.domain.po.ThreadPool.PoolUpdateConfig;
import com.guilu.Manager.updater.PoolUpdater;
import com.guilu.exception.BusinessException.ThreadPoolException;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 当前实例的线程池更新器。跨服务传输由独立的分发器负责。 */
@RequiredArgsConstructor
public class TpConfigUpdater implements TPUpdater {
    private final ApplicationEventPublisher publisher;
    private final NacosRegistration registration;

    @Override
    public boolean publishTPConfigEvent(TPConfigUpdaterEvent event) {
        if (event == null || event.getPoolConfig() == null) {
            throw new ThreadPoolException("线程池更新事件缺少配置");
        }
        PoolUpdateConfig config = event.getPoolConfig();
        if ((config.getService() != null && !config.getService().equals(registration.getServiceId()))
                || (config.getHost() != null && !config.getHost().equals(registration.getHost()))
                || (config.getPort() != null && config.getPort() != registration.getPort())) {
            //todo 跨服务分发线程池更新配置
        }
        // 冻结事件参数，防止调用方在事务提交前继续修改配置对象。
        PoolUpdateConfig copy = new PoolUpdateConfig();
        BeanUtils.copyProperties(config, copy);
        PoolUpdater.of().validate(copy);
        TPConfigUpdaterEvent pending = new TPConfigUpdaterEvent(event.getSource(), copy);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.publishEvent(pending);
                }
            });
        } else {
            publisher.publishEvent(pending);
        }
        return true;
    }

    @Override
    public boolean updateTP(BaseThreadPoolConfig threadPoolConfig) {
        PoolUpdater.of().update(threadPoolConfig);
        return true;
    }
}
