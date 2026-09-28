package com.guilu.threadPool;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义拒绝策略
 * 实现拒绝任务的处理，并支持告警事件发布
 *
 * @author 归鹭
 * 春落蝉鸣夜，结友载酒归。
 * 风幽白玉宫，彩云照明出。
 */
@Slf4j
public class DynamicRejectHandler implements RejectedExecutionHandler {

    public DynamicRejectHandler() {
    }

    private final AtomicInteger count = new AtomicInteger(0);

    /**
     * 线程池ID（用于标识）
     */
    private Long poolId;

    /**
     * 线程池名称
     */
    private String poolName;

    /**
     * 设置线程池信息
     */
    public void setPoolInfo(Long poolId, String poolName) {
        this.poolId = poolId;
        this.poolName = poolName;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // 1. 拒绝次数自增
        long rejectCount = count.incrementAndGet();

        // 2. 发布告警事件

        // 3. 记录日志
        log.info("Rejected task: {}, rejectCount: {}", r.toString(), rejectCount);
    }

    /**
     * 获取拒绝次数
     */
    public int getCount() {
        return count.get();
    }

    /**
     * 重置计数
     */
    public  void resetCount() {
        count.set(0);
    }

    /**
     * AbortPolicy - 默认直接抛出异常
     */
    public class AbortPolicy implements RejectedExecutionHandler {
        public AbortPolicy() {
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            count.incrementAndGet();

            throw new RejectedExecutionException("Task " + r.toString() +
                    " rejected from " + e.toString());
        }
    }

    /**
     * DiscardPolicy - 不进行处理拒绝的任务
     */
    public class DiscardPolicy implements RejectedExecutionHandler {
        public DiscardPolicy() {
        }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            count.incrementAndGet();

            // 发布告警事件

            log.debug("Task {} rejected and discarded", r.toString());
        }
    }

    /**
     * DiscardOldestPolicy - 丢弃队列中最老的任务，执行当前任务
     */
    public class DiscardOldestPolicy implements RejectedExecutionHandler {
        public DiscardOldestPolicy() {
        }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            count.incrementAndGet();

            // 发布告警事件

            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }

    /**
     * CallerRunsPolicy - 只用调用者的线程运行任务
     */
    public class CallerRunsPolicy implements RejectedExecutionHandler {
        public CallerRunsPolicy() {
        }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            count.incrementAndGet();

            // 发布告警事件

            if (!e.isShutdown()) {
                r.run();
            }
        }
    }
}