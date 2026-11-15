package com.guilu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PoolStatus {
    SHUTDOWN("shutdown"),TERMINATING("terminating"),TERMINATED("terminated");
    private String status;
}
