package com.guilu.DynamicThreadPool.FLDynamicReject;

import com.guilu.Event.TPRejectEvent;
import com.guilu.EventPublisher.TpPublisher.TPRejectPublisher;
import com.guilu.enums.RejectHandlerType;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自定义拒绝策略。
 * @author 归鹭
 * 春落蝉鸣夜，结友载酒归。
 * 风幽白玉宫，彩云照明出。
 */
@Slf4j
public class DynamicRejectHandler implements RejectedExecutionHandler, RejectionStatsSource {

    private final RejectHandlerType type;
    private final RejectedExecutionHandler delegate;
    private final AtomicLong count = new AtomicLong(0);

    /**
     * 线程池 ID
     */
    private volatile Long poolId;

    /**
     * 线程池名称
     */
    private volatile String poolName;

    public DynamicRejectHandler() {
        this(RejectHandlerType.DYNAMIC);
    }

    public DynamicRejectHandler(RejectHandlerType type) {
        this.type = Objects.requireNonNull(type, "type");
        this.delegate = delegateFor(type);
    }

    private static RejectedExecutionHandler delegateFor(RejectHandlerType type) {
        return switch (type) {
            case CALLERRUNS -> new CallerRuns();
            case DISCARD -> (task, executor) -> {};
            case DISCARDOLDEST -> new DiscardOldest();
            case ABORT -> (task, executor) -> {
                throw new RejectedExecutionException("Task " + task + " rejected from " + executor);
            };
            case DYNAMIC -> (task, executor) -> {
                throw new RejectedExecutionException("Task rejected from " + executor);
            };
        };
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        long rejectCount = count.incrementAndGet();
        publishRejectEvent(r, executor, type);
        log.info("Rejected task: {}, poolId: {}, poolName: {}, type: {}, rejectCount: {}",
                r, poolId, poolName, type, rejectCount);
        delegate.rejectedExecution(r, executor);
    }

    @Override
    public long rejectedCount() {
        return count.get();
    }

    public RejectHandlerType getType() {
        return type;
    }

    /**
     * 获取拒绝次数。
     */
    public long getCount() {
        return rejectedCount();
    }

    /**
     * 重置计数。
     */
    public void resetCount() {
        count.set(0);
    }

    /**
     * 设置线程池信息。策略类型在构造时固定，传入不同类型只会记录告警。
     */
    public void setPoolInfo(Long poolId, String poolName, RejectHandlerType type) {
        this.poolId = poolId;
        this.poolName = poolName;
        if (type != null && type != this.type) {
            log.warn("Reject handler type is immutable: configured={}, requested={}", this.type, type);
        }
    }

    private void publishRejectEvent(Runnable r, ThreadPoolExecutor e, RejectHandlerType type) {
        TPRejectPublisher.publishRejectEvent(
                TPRejectEvent.createEvent(r, getCount(), type.getRejectType(), e));
    }

    /**
     * CallerRunsPolicy - 使用调用者线程执行任务。
     */
    private static final class CallerRuns implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    /**
     * DiscardOldestPolicy - 丢弃队列中最老的任务，再尝试执行当前任务。
     */
    private static final class DiscardOldest implements RejectedExecutionHandler {
        private final ThreadLocal<Boolean> retrying = ThreadLocal.withInitial(() -> false);
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (e.isShutdown()) {
                return;
            }
            if (retrying.get() || e.getQueue().poll() == null) {
                throw new RejectedExecutionException("Cannot discard oldest and enqueue task");
            }
            retrying.set(true);
            try {
                e.execute(r);
            } finally {
                retrying.remove();
            }
        }
    }
}
