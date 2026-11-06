package com.guilu.domain.po.ThreadPool;

import java.io.Serial;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.guilu.enums.ThreadFactoryType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommonThreadPoolConfig extends BaseThreadPoolConfig {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String blockingQueueType;

    private ThreadFactoryType threadFactory;
    private Long createUser;
    private LocalDateTime createTime;
    @TableField("`delete`")
    private Integer delete;

}
