package com.guilu.mapper;

import com.guilu.domain.po.TaskExecution;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 任务执行记录 Mapper 接口
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Mapper
public interface TaskExecutionMapper extends BaseMapper<TaskExecution> {

}
