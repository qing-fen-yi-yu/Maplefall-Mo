package com.guilu.enums;

import com.guilu.utils.StringUtils;

public enum TPType {
    DYNAMIC("dynamic"),DEFAULT("default");
    private final String executorType;
    TPType(String executorType) {
        this.executorType = executorType;
    }
    public static TPType of(String executorType) {
        for (TPType tpType : TPType.values()) {
            if (StringUtils.equals(tpType.executorType, executorType)) {
                return tpType;
            }
        }
        return null;
    }
}
