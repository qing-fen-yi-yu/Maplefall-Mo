package com.guilu.monitor.domain;

public record BufferPoolInfo(
        String name, // buffer pool 名称
        Long count, // 缓冲区估计值
        Long totalCap, //缓冲区总容量估计
        Long memUsed // 缓冲区在内存中的占用
) {
}
