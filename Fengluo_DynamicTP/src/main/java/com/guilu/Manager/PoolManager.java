package com.guilu.Manager;

import com.guilu.exception.BusinessException.BadDataException;
import com.guilu.exception.BusinessException.ThreadPoolException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PoolManager {
    static final Map<String, Executor> POOL_REGISTRY = new ConcurrentHashMap<>();
    static final Map<String, Executor> ALIVE_REGISTRY = new ConcurrentHashMap<>();
    static final AtomicInteger COUNT = new AtomicInteger();
    protected Map<String, Executor> getAlivePool() {
        return Map.copyOf(ALIVE_REGISTRY);
    }
    public int getRegisPoolCount() {
        return COUNT.get();
    }
    public int getCurCount() {
        return POOL_REGISTRY.size();
    }
    public int getAliveCount() {
        return ALIVE_REGISTRY.size();
    }
    protected static Executor getPoolByAll(String name) {
        return POOL_REGISTRY.get(name);
    }
    protected static Executor getPoolByAlive(String name) {
        return ALIVE_REGISTRY.get(name);
    }
    protected static synchronized Executor addRegistry(String name, Object executor) {
        if (name == null || name.isBlank()) throw new BadDataException("Pool name required");
        if (!(executor instanceof ExecutorService pool)) throw new BadDataException("ExecutorService required");
        if (pool.isShutdown()) throw new BadDataException("Cannot register a shutdown pool");
        Executor previous = POOL_REGISTRY.putIfAbsent(name, pool);
        if (previous != null && previous != pool) throw new BadDataException("Duplicate pool name: " + name);
        ALIVE_REGISTRY.put(name, pool);
        return previous;
    }
    protected static void incrementCount() {
        COUNT.incrementAndGet();
    }
    protected static void removeExecutorByAlive(String name) {
        ALIVE_REGISTRY.remove(name);
    }
    protected synchronized void removePoolByRegis(String name, Object pool) {
        if (name == null || pool == null) return;
        ALIVE_REGISTRY.remove(name, pool);
        POOL_REGISTRY.remove(name, pool);
    }
}
