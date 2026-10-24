package com.guilu.domain;

//todo 弥补Gc信息的详细性
public record  GcInfo(
        Long collectionCount,//垃圾回收器次数
        Long collectionTime, //垃圾回收器的收集时间
        String[] memPoolNames, //所管理的内存池名称
        String name //垃圾回收期名称
) {
}
