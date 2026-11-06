package com.guilu.mapper;

import com.guilu.domain.po.ThreadPoolConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 动态线程池配置表 Mapper 接口
 * </p>
 *
 * @author 归鹭
 * @since 2026-10-13
 */
@Mapper
public interface ThreadPoolConfigMapper extends BaseMapper<ThreadPoolConfig> {
    /** 同一配置的快照版本通过配置行锁串行分配。 */
    @Select("SELECT id FROM thread_pool_config WHERE id = #{id} FOR UPDATE")
    Long lockConfig(@Param("id") Long id);
}
