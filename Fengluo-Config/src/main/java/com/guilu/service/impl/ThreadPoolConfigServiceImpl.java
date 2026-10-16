package com.guilu.service.impl;

import com.guilu.domain.po.ThreadPoolConfig;
import com.guilu.mapper.ThreadPoolConfigMapper;
import com.guilu.service.IThreadPoolConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 动态线程池配置表 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@Service
public class ThreadPoolConfigServiceImpl extends ServiceImpl<ThreadPoolConfigMapper, ThreadPoolConfig> implements IThreadPoolConfigService {

}
