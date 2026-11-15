package com.guilu.service.impl;

import com.guilu.domain.po.TaskDefinition;
import com.guilu.mapper.TaskDefinitionMapper;
import com.guilu.service.ITaskDefinitionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guilu.service.ITaskGroupRelationService;
import com.guilu.service.ITaskGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * <p>
 * 任务定义 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Service
@RequiredArgsConstructor
public class TaskDefinitionServiceImpl extends ServiceImpl<TaskDefinitionMapper, TaskDefinition> implements ITaskDefinitionService {
    private final ITaskGroupRelationService relationService;
    private final ITaskGroupService taskGroupService;

    @Override
    public void addTaskDefinition(TaskDefinition taskDefinition) {
    }

    @Override
    public boolean removeById(Serializable id) {

        return super.removeById(id);
    }

    private boolean validTaskDefinitionData(TaskDefinition taskDefinition){
        return true;
    }
}
