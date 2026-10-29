package com.guilu.monitor.sdk.constants;

public interface MetricNames {
    interface EXECUTOR{
        String EXECUTOR_PREFIX = "fengluo.executor.";
        String EXECUTOR_SIZE = EXECUTOR_PREFIX + "size";
        String EXECUTOR_CORE = EXECUTOR_PREFIX + "core";
        String EXECUTOR_MAX = EXECUTOR_PREFIX + "max";
        String EXECUTOR_LARGEST = EXECUTOR_PREFIX + "largest";
        String EXECUTOR_ACTIVE = EXECUTOR_PREFIX + "active";
        String EXECUTOR_QUEUE_SIZE = EXECUTOR_PREFIX + "queue.size";
        String EXECUTOR_QUEUE_REMAINING = EXECUTOR_PREFIX + "queue.remaining";
        String EXECUTOR_QUEUE_CAPACITY = EXECUTOR_PREFIX + "queue.capacity";
        String EXECUTOR_TASKS = EXECUTOR_PREFIX + "tasks";
        String EXECUTOR_COMPLETED = EXECUTOR_PREFIX + "completed";
        String EXECUTOR_REJECTED = EXECUTOR_PREFIX + "rejected";
        String EXECUTOR_KEEP_ALIVE = EXECUTOR_PREFIX + "keep.alive";
        String EXECUTOR_STATUS = EXECUTOR_PREFIX + "status";
        String EXECUTOR_INFO = EXECUTOR_PREFIX + "info";
    }
     interface JVM{
         String JVM_THREADS_DEADLOCKED = "fengluo.jvm.threads.deadlocked";
         String JVM_THREADS_COUNT = "fengluo.jvm.threads.count";
         String JVM_THREADS_DAEMON = "fengluo.jvm.threads.daemon";
         String JVM_THREADS_PEAK = "fengluo.jvm.threads.peak";
         String JVM_THREADS_STARTED = "fengluo.jvm.threads.started";
         String JVM_CLASSES_LOADED = "fengluo.jvm.classes.loaded";
         String JVM_CLASSES_UNLOADED = "fengluo.jvm.classes.unloaded";
         String JVM_MEMORY_PENDING_FINALIZATION = "fengluo.jvm.memory.pending.finalization";
         String JVM_GC_COUNT = "fengluo.jvm.gc.collection.count";
         String JVM_GC_TIME = "fengluo.jvm.gc.collection.time";
         String JVM_GC_EVENT = "fengluo.jvm.gc.event";
         String JVM_BUFFER_COUNT = "fengluo.jvm.buffer.count";
         String JVM_BUFFER_MEMORY_USED = "fengluo.jvm.buffer.memory.used";
         String JVM_COMPILATION_TIME = "fengluo.jvm.compilation.time";
         String JVM_MEMORY_RUNTIME_TOTAL = "fengluo.jvm.memory.runtime.total";
         String JVM_MEMORY_RUNTIME_FREE = "fengluo.jvm.memory.runtime.free";
         String JVM_MEMORY_POOL_GAUGE = "fengluo.jvm.memory.pool.";
         String JVM_MEMORY_GAUGE = "fengluo.jvm.memory.";
         String MEMORY_POOL_THRESHOLD_GAUGE = "fengluo.jvm.memory.pool.threshold";
     }
     interface SYSTEM {
         String SYSTEM_CPU_COUNT = "fengluo.system.cpu.count";
         String SYSTEM_CPU_USER = "fengluo.system.cpu.user";
         String SYSTEM_CPU_SYSTEM = "fengluo.system.cpu.system";
         String SYSTEM_CPU_IDLE = "fengluo.system.cpu.idle";
         String SYSTEM_CPU_WAIT = "fengluo.system.cpu.wait";
         String SYSTEM_CPU_TOTAL = "fengluo.system.cpu.total";
         String SYSTEM_CPU_WAIT_TIME = "fengluo.system.cpu.wait.time";
         String SYSTEM_MEMORY_USED = "fengluo.system.memory.used";
         String SYSTEM_MEMORY_USAGE = "fengluo.system.memory.usage";
         String SYSTEM_CPU_LOAD = "fengluo.system.sys.cpu.load";
         String PROCESS_CPU_LOAD = "fengluo.system.process.cpu.load";
         String SYSTEM_LOAD_AVERAGE = "fengluo.system.load.avg";
         String SYSTEM_SWAP_TOTAL = "fengluo.system.swap.total";
         String SYSTEM_SWAP_FREE = "fengluo.system.swap.free";
         String PROCESS_FD_OPEN = "fengluo.system.fd.open";
         String PROCESS_FD_MAX = "fengluo.system.fd.max";
     }
}
