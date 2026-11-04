package com.guilu.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;

public class JsonUtils extends JSONUtil {
    public static <T> T toBean(String jsonString, TypeReference<T> typeReference) {
        try {
            return JsonUtils.toBean(JsonUtils.parse(jsonString), typeReference,false);
        } catch (Exception e) {
            throw new RuntimeException("JSON 反序列化失败: " + jsonString, e);
        }
    }
}