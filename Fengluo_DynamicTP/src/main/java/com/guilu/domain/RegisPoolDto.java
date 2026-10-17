package com.guilu.domain;

import com.guilu.annotation.DynamicThreadPool;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisPoolDto {
    private Object pool;
    private DynamicThreadPool anno;
    private String beanName;

    public  RegisPoolDto(Object pool, DynamicThreadPool anno) {
        this.pool = pool;
        this.anno = anno;
    }
    public  RegisPoolDto(Object pool, DynamicThreadPool anno,String beanName) {
        this.pool = pool;
        this.anno = anno;
        this.beanName = beanName;
    }
    public static RegisPoolDto of(Object pool,DynamicThreadPool anno) {
        return  new RegisPoolDto(pool,anno);
    }
    public static RegisPoolDto of(Object pool,DynamicThreadPool anno,String beanName){
        return  new RegisPoolDto(pool,anno,beanName);
    }
}
