package com.guilu.Manager;

import com.guilu.Manager.updater.AutoDestroyTP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class PoolLifecycle implements SmartLifecycle {
    //线程池关闭的最大等待事件
    private static final long AWAIT_SECONDS = 5L;

    // 高 phase 在关闭时先停止，在 Bean 销毁之前执行。
    @Override
    public int getPhase() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private volatile boolean running = false;

    @Override
    public void start() {
        this.running = true;
        AutoDestroyTP autoDestroyTP = AutoDestroyTP.of();
        autoDestroyTP.checkDown();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void stop() {
        stop(() -> {
        });
    }

    @Override
    public void stop(Runnable callback) {
        try {
            AutoDestroyTP.of().stop();
            doShutdownAll();
        } catch (Throwable t) {
            log.error("关闭线程池时发生异常", t);
        } finally {
            running = false;
            PoolManager.ALIVE_REGISTRY.clear();
            PoolManager.POOL_REGISTRY.clear();
            callback.run();//回调spring接口发送结束通知
        }
    }

    private void doShutdownAll() {
        Map<String, Executor> registry = PoolManager.ALIVE_REGISTRY;
        if (registry.isEmpty()) {
            return;
        }
        List<Map.Entry<String, Executor>> snapshot = List.copyOf(registry.entrySet());
        for (Map.Entry<String, Executor> e : snapshot) {
            String key = e.getKey();
            ExecutorService es = (ExecutorService) e.getValue();
            log.info("poolname:{}----{}shutdown",key,es);
            try { es.shutdown(); }
            catch (RuntimeException ex) { log.error("线程池 {} 关闭失败", key, ex); }
        }
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(AWAIT_SECONDS);
        for (Map.Entry<String, Executor> e : snapshot) {
            String name = e.getKey();
            if (!(e.getValue() instanceof ExecutorService es)) {
                continue;
            }
            try {
                if (!es.awaitTermination(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                    forceShutdownNow(name, es);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                forceShutdownNow(name, es);
            }
        }
        }


        private void forceShutdownNow (String name, ExecutorService es){
            try {
                List<Runnable> dropped = es.shutdownNow();
                for (Runnable task : dropped) {
                    if (task instanceof Future<?> future) future.cancel(false);
                }
                log.warn("线程池{}强制终止，存在{}个未执行任务", name, dropped.size());
            } catch (Throwable t) {
                log.error("{}线程池关闭失败", name, t);
            }
        }
}