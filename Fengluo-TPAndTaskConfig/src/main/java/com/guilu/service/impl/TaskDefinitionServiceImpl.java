package com.guilu.service.impl;

import com.guilu.domain.po.TaskDefinition;
import com.guilu.mapper.TaskDefinitionMapper;
import com.guilu.service.ITaskDefinitionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 任务定义 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Service
public class TaskDefinitionServiceImpl extends ServiceImpl<TaskDefinitionMapper, TaskDefinition> implements ITaskDefinitionService {

}
