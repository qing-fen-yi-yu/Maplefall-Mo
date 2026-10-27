package com.guilu.domain;

import com.guilu.MXBean;
import lombok.Getter;

import java.lang.management.CompilationMXBean;

/**
 * 应用编译信息。
 */
@Getter
public final class AppCompileInfo {

    private static volatile AppCompileInfo instance;

    /** JIT 编译器名称;无 JIT 环境为 null。 */
    private final String jitName;

    /** 快照时刻的累计编译耗时(毫秒);无 JIT 环境为 -1。 */
    private final long totalCompileTime;

    private AppCompileInfo() {
        CompilationMXBean bean = MXBean.getCompilationBean();
        if (bean == null) {
            this.jitName = null;
            this.totalCompileTime = -1L;
        } else {
            this.jitName = bean.getName();
            this.totalCompileTime = bean.getTotalCompilationTime();
        }
    }

    /**
     * 获取应用编译信息单例。首次调用时完成 JIT 信息快照,后续直接返回缓存。
     */
    public static AppCompileInfo of() {
        AppCompileInfo local = instance;
        if (local == null) {
            synchronized (AppCompileInfo.class) {
                local = instance;
                if (local == null) {
                    local = new AppCompileInfo();
                    instance = local;
                }
            }
        }
        return local;
    }
}
