package com.guilu.domain;

/**
 * GC 指标信息，时间默认以ms显示
 */
public record GcInfo(Long collectionCount, Long collectionTime, String[] memPoolNames,
        String name, Long startTime, String gcCause) {
    public GcInfo { memPoolNames = memPoolNames.clone(); }
    public GcInfo(Long count, Long time, String[] pools, String name) {
        this(count, time, pools, name, null, null);
    }
    @Override public String[] memPoolNames() { return memPoolNames.clone(); }
}
