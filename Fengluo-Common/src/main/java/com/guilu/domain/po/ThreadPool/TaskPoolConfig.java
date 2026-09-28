package com.guilu.domain.po.ThreadPool;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
public class TaskPoolConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String threadPoolName;

    private Integer coreSize;

    private Integer maxPoolSize;

    private Integer keepAliveTime;

    private String timeUnit;

    private String rejectHandler;

    private String blockingQueueType;

    private Integer blockingQueueSize;

    private String threadFactory;
    private Long createUser;
    private LocalDateTime createTime;
    @TableField("`delete`")
    private Integer delete;

    @Override
    public String toString() {
        return "TaskPoolConfig{" +
                "id=" + id +
                ", threadPoolName='" + threadPoolName + '\'' +
                ", coreSize=" + coreSize +
                ", maxPoolSize=" + maxPoolSize +
                ", keepAliveTime=" + keepAliveTime +
                ", timeUnit='" + timeUnit + '\'' +
                ", rejectHandler='" + rejectHandler + '\'' +
                ", blockingQueueType='" + blockingQueueType + '\'' +
                ", blockingQueueSize=" + blockingQueueSize +
                ", threadFactory='" + threadFactory + '\'' +
                ", createUser=" + createUser +
                ", createTime=" + createTime+
                '}';
    }
}
