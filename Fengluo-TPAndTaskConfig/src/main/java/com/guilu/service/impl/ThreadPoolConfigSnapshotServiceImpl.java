package com.guilu.service.impl;

import com.guilu.Listener.TPConfigListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.domain.po.ThreadPoolConfig;
import com.guilu.domain.po.ThreadPoolConfigSnapshot;
import com.guilu.domain.po.ThreadPool.BaseThreadPoolConfig;
import com.guilu.exception.BusinessException.ThreadPoolException;
import com.guilu.mapper.ThreadPoolConfigMapper;
import com.guilu.mapper.ThreadPoolConfigSnapshotMapper;
import com.guilu.service.IThreadPoolConfigSnapshotService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.utils.CollUtils;
import com.guilu.utils.JsonUtils;
import com.guilu.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 线程池配置快照表 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@Service
@RequiredArgsConstructor
public class ThreadPoolConfigSnapshotServiceImpl extends ServiceImpl<ThreadPoolConfigSnapshotMapper, ThreadPoolConfigSnapshot> implements IThreadPoolConfigSnapshotService {
    private final TPConfigListener tpConfigListener;
    private final ThreadPoolConfigMapper configMapper;
    @Override
    public Map<String, Set<DynamicPoolSnapshot>> queryAll() {
        Map<String, Set<DynamicPoolSnapshot>> map = tpConfigListener.getDynamicPoolSnapshotMap();
        if (map.isEmpty()) {
            return CollUtils.emptyMap();
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void TPConfigToSna(ThreadPoolConfig threadPoolConfig) {
        if (threadPoolConfig == null || threadPoolConfig.getId() == null) {
            throw new ThreadPoolException("记录快照前必须先保存线程池配置");
        }
        Long configId = threadPoolConfig.getId();
        if (configMapper.lockConfig(configId) == null) {
            throw new ThreadPoolException("线程池配置不存在: " + configId);
        }
        ThreadPoolConfigSnapshot previous = baseMapper.selectOne(new LambdaQueryWrapper<ThreadPoolConfigSnapshot>()
                .eq(ThreadPoolConfigSnapshot::getConfigId, configId)
                .orderByDesc(ThreadPoolConfigSnapshot::getVersion)
                .last("limit 1 for update"));
        ThreadPoolConfigSnapshot snapshot = new ThreadPoolConfigSnapshot();
        BeanUtils.copyProperties(threadPoolConfig, snapshot, BaseThreadPoolConfig.class);
        snapshot.setConfigId(configId);
        snapshot.setVersion(previous == null ? 1 : Math.addExact(previous.getVersion(), 1));
        snapshot.setChangeType(previous == null ? "CREATE" : "UPDATE");
        Long changedBy = UserContext.getUser();
        if (changedBy == null) {
            changedBy = threadPoolConfig.getUpdatedBy() == null
                    ? threadPoolConfig.getCreatedBy() : threadPoolConfig.getUpdatedBy();
        }
        snapshot.setChangedBy(changedBy);
        snapshot.setChangedAt(LocalDateTime.now());
        snapshot.setConfigJson(JsonUtils.toJsonStr(threadPoolConfig));
        if (!save(snapshot)) {
            throw new ThreadPoolException("保存线程池配置快照失败: " + configId);
        }
    }
}
