package com.guilu.Manager.inquirer;

import com.guilu.Manager.PoolManager;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executor;

//线程池查询器
public class PoolInquirer extends PoolManager {
    private static final PoolInquirer inquirer = new PoolInquirer();
    public Map<String, Executor> getAllAlivePool(){
        return super.getAlivePool();
    }
    public Executor getPool(String name) { return getPoolByAlive(name); }
    public static PoolInquirer of(){
        return  inquirer;
    }
}
