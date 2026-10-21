package com.guilu.mapper;

import com.guilu.domain.po.TaskGroupRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 任务组-任务关联表 Mapper 接口
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-12
 */
@Mapper
public interface TaskGroupRelationMapper extends BaseMapper<TaskGroupRelation> {

}
