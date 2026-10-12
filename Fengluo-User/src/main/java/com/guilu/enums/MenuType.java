package com.guilu.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum MenuType {
    D("dic",1),M("menu",2),B("button",3);
    private final String type;
    @EnumValue
    @JsonValue
    private final Integer id;

    MenuType(String type, Integer id) {
        this.type = type;
        this.id = id;
    }

}
