package com.guilu.domain.Enum;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CodeTypeEnum  {
    LINECAPTCHA("line",1),CIRCLECAPTCHA("circle",2),SHEARCAPTCHA("shear",3);
    private String type;
    @JsonValue
    private Integer typeId;

    CodeTypeEnum(String type, Integer id) {
        this.type = type;
        this.typeId = id;
    }
    public String of(Integer value){
        for (CodeTypeEnum codeTypeEnum : CodeTypeEnum.values()) {
            if (codeTypeEnum.typeId.equals(value)) {
                return  codeTypeEnum.type;
            }
        }
        return null;
    }
}
