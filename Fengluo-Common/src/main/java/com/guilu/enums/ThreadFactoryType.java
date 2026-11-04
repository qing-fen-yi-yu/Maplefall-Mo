package com.guilu.enums;

import lombok.Getter;

@Getter
public enum ThreadFactoryType {
    DEFAULT("default"),DYNAMIC("dynamic");
    private String factoryName;
    ThreadFactoryType(String factoryName){
        factoryName = factoryName.toUpperCase();
    }
}
