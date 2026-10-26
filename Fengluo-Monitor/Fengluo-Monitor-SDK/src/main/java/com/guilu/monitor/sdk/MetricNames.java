package com.guilu.monitor.sdk;

public final class MetricNames {

    private MetricNames() {
    }

    public static final String EXECUTOR_PREFIX = "fengluo.executor.";
    public static final String EXECUTOR_SIZE = EXECUTOR_PREFIX + "size";
    public static final String EXECUTOR_CORE = EXECUTOR_PREFIX + "core";
    public static final String EXECUTOR_MAX = EXECUTOR_PREFIX + "max";
    public static final String EXECUTOR_LARGEST = EXECUTOR_PREFIX + "largest";
    public static final String EXECUTOR_ACTIVE = EXECUTOR_PREFIX + "active";
    public static final String EXECUTOR_QUEUE_SIZE = EXECUTOR_PREFIX + "queue.size";
    public static final String EXECUTOR_QUEUE_REMAINING = EXECUTOR_PREFIX + "queue.remaining";
    public static final String EXECUTOR_QUEUE_CAPACITY = EXECUTOR_PREFIX + "queue.capacity";
    public static final String EXECUTOR_TASKS = EXECUTOR_PREFIX + "tasks";
    public static final String EXECUTOR_COMPLETED = EXECUTOR_PREFIX + "completed";
    public static final String EXECUTOR_REJECTED = EXECUTOR_PREFIX + "rejected";
    public static final String EXECUTOR_KEEP_ALIVE = EXECUTOR_PREFIX + "keep.alive";
    public static final String EXECUTOR_STATUS = EXECUTOR_PREFIX + "status";
    public static final String EXECUTOR_INFO = EXECUTOR_PREFIX + "info";

    public static final String SYSTEM_CPU_LOAD = "fengluo.system.cpu.load";
    public static final String PROCESS_CPU_LOAD = "fengluo.process.cpu.load";
    public static final String SYSTEM_LOAD_AVERAGE = "fengluo.system.load.average";
    public static final String SYSTEM_SWAP_TOTAL = "fengluo.system.swap.total";
    public static final String SYSTEM_SWAP_FREE = "fengluo.system.swap.free";
    public static final String PROCESS_FD_OPEN = "fengluo.process.fd.open";
    public static final String PROCESS_FD_MAX = "fengluo.process.fd.max";

    public static final String JVM_THREADS_DEADLOCKED = "fengluo.jvm.threads.deadlocked";
    public static final String JVM_THREADS_COUNT = "fengluo.jvm.threads.count";
    public static final String JVM_THREADS_DAEMON = "fengluo.jvm.threads.daemon";
    public static final String JVM_THREADS_PEAK = "fengluo.jvm.threads.peak";
    public static final String JVM_THREADS_STARTED = "fengluo.jvm.threads.started";
    public static final String JVM_CLASSES_LOADED = "fengluo.jvm.classes.loaded";
    public static final String JVM_CLASSES_UNLOADED = "fengluo.jvm.classes.unloaded";
    public static final String JVM_MEMORY_PENDING_FINALIZATION =
            "fengluo.jvm.memory.pending.finalization";
    public static final String JVM_GC_COUNT = "fengluo.jvm.gc.collection.count";
    public static final String JVM_GC_TIME = "fengluo.jvm.gc.collection.time";
    public static final String JVM_GC_EVENT = "fengluo.jvm.gc.event";
    public static final String JVM_BUFFER_COUNT = "fengluo.jvm.buffer.count";
    public static final String JVM_BUFFER_MEMORY_USED = "fengluo.jvm.buffer.memory.used";
    public static final String JVM_COMPILATION_TIME = "fengluo.jvm.compilation.time";

    public static final String SYSTEM_CPU_COUNT = "fengluo.system.cpu.count";
    public static final String SYSTEM_CPU_USER = "fengluo.system.cpu.user";
    public static final String SYSTEM_CPU_SYSTEM = "fengluo.system.cpu.system";
    public static final String SYSTEM_CPU_IDLE = "fengluo.system.cpu.idle";
    public static final String SYSTEM_CPU_WAIT = "fengluo.system.cpu.wait";
    public static final String SYSTEM_CPU_TOTAL = "fengluo.system.cpu.total";
    public static final String SYSTEM_CPU_WAIT_TIME = "fengluo.system.cpu.wait.time";
    public static final String SYSTEM_MEMORY_USED = "fengluo.system.memory.used";
    public static final String SYSTEM_MEMORY_USAGE = "fengluo.system.memory.usage";
    public static final String JVM_MEMORY_RUNTIME_TOTAL = "fengluo.jvm.memory.runtime.total";
    public static final String JVM_MEMORY_RUNTIME_FREE = "fengluo.jvm.memory.runtime.free";
}
