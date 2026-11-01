package com.guilu.service.impl;

import com.guilu.domain.po.TaskExecution;
import com.guilu.mapper.TaskExecutionMapper;
import com.guilu.service.ITaskExecutionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 任务执行记录 服务实现类
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Service
public class TaskExecutionServiceImpl extends ServiceImpl<TaskExecutionMapper, TaskExecution> implements ITaskExecutionService {

}
