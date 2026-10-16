package com.guilu.domain;

import com.guilu.annotation.DynamicThreadPool;

public class RegisPoolDto {
    private Object pool;
    private DynamicThreadPool anno;

    public  RegisPoolDto(Object pool, DynamicThreadPool anno) {
        this.pool = pool;
        this.anno = anno;
    }

    public Object getPool() {
        return pool;
    }

    public void setPool(Object pool) {
        this.pool = pool;
    }

    public DynamicThreadPool getAnno() {
        return anno;
    }

    public void setAnno(DynamicThreadPool anno) {
        this.anno = anno;
    }
    public static RegisPoolDto of(Object pool,DynamicThreadPool anno) {
        return  new RegisPoolDto(pool,anno);
    }
}
