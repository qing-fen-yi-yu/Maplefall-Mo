package com.guilu.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guilu.Listener.TPConfigListener;
import com.guilu.domain.DynamicPoolSnapshot;
import com.guilu.domain.dto.TPConfigPageQueryDto;
import com.guilu.domain.po.ThreadPoolConfig;
import com.guilu.domain.query.QueryPage;
import com.guilu.mapper.ThreadPoolConfigMapper;
import com.guilu.service.IThreadPoolConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.utils.CollUtils;
import com.guilu.utils.queryUtil.QueryCreateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final TPConfigListener tpConfigListener;
    @Override
    public Page<ThreadPoolConfig> pageQuery(TPConfigPageQueryDto queryDto) {
        QueryPage queryPage = queryDto.getPage();
        Page<ThreadPoolConfig> page = new Page<>(queryPage.getCurrent(), queryPage.getPageSize());
        page = page(page,queryCreateUtils.generateQueryWrapper(queryDto));
        return page;
    }

    @Override
    public Map<String, Set<DynamicPoolSnapshot>> pageQueryAll() {
        Map<String, Set<DynamicPoolSnapshot>> map = tpConfigListener.getDynamicPoolSnapshotMap();
        if (map.isEmpty()) {
            return CollUtils.emptyMap();
        }
        return map;
    }

    @Override
    public ThreadPoolConfig getAllById(Long id) {
        return null;
    }
}
