package com.guilu.Manager.updater;

import com.guilu.Manager.inquirer.PoolInquirer;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.concurrent.*;

//线程池自动注销器
@Slf4j
public class AutoDestroyTP {
    private static final AutoDestroyTP INSTANCE = new AutoDestroyTP();
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private long checkTimes = 10000;
    private ScheduledExecutorService scheduler;
    public static AutoDestroyTP of() { return INSTANCE; }
    private AutoDestroyTP() { }
    public synchronized void setUnit(TimeUnit unit) { this.unit = Objects.requireNonNull(unit); }
    public synchronized void setCheckTimes(Long interval) {
        if (interval == null || interval <= 0) throw new IllegalArgumentException("Positive interval required");
        checkTimes = interval;
    }
    public synchronized void checkDown() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "fengluo-pool-cleaner");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::cleanNow, 0, checkTimes, unit);
    }
    public void cleanNow() {
        PoolInquirer.of().getAllAlivePool().forEach((name, executor) -> {
            if (executor instanceof ExecutorService service && service.isTerminated())
                PoolUpdater.of().removePoolByBean(name, executor);
        });
    }
    public synchronized void stop() {
        if (scheduler != null) { scheduler.shutdownNow(); scheduler = null; }
    }
}
