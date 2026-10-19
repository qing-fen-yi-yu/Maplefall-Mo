package com.guilu.DynamicThreadPool.FLDynamicReject;

import com.guilu.Event.TPRejectEvent;
import com.guilu.EventPublisher.TpPublisher.TPRejectPublisher;
import com.guilu.enums.RejectHandlerType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

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
    /**
     * 线程池ID（用于标识）
     */
    private Long poolId;

    /**
     * 线程池名称
     */
    private String poolName;
    //拒绝策略计数器
    private final AtomicLong count = new AtomicLong(0);
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        long rejectCount = count.incrementAndGet();
        publishRejectEvent(r,executor,RejectHandlerType.DYNAMIC);
        log.info("Rejected task: {}, rejectCount: {}", r.toString(), rejectCount);
        throw new RejectedExecutionException("Task rejected from " + executor);
    }

    public RejectedExecutionHandler getCallerRunsPolicy() {
        return new CallerRunsPolicy();
    }

    public RejectedExecutionHandler getDiscardOldestPolicy() {
        return new DiscardOldestPolicy();
    }

    public RejectedExecutionHandler getDiscardPolicy() {
        return  new DiscardPolicy();
    }

    public RejectedExecutionHandler getAbortPolicy() {
        return  new AbortPolicy();
    }

    /**
     * AbortPolicy - 默认直接抛出异常
     */
    private class AbortPolicy implements RejectedExecutionHandler {
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
    private class DiscardPolicy implements RejectedExecutionHandler {
        public DiscardPolicy() {
        }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            count.incrementAndGet();
            //发布告警事件
            publishRejectEvent(r,e,RejectHandlerType.DISCARD);
            log.debug("Task {} rejected and discarded", r.toString());
        }
    }

    /**
     * DiscardOldestPolicy - 丢弃队列中最老的任务，执行当前任务
     */
    private class DiscardOldestPolicy implements RejectedExecutionHandler {
        private final ThreadLocal<Boolean> retrying = ThreadLocal.withInitial(() -> false);
        public DiscardOldestPolicy() {
        }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            count.incrementAndGet();
            // 发布告警事件
            publishRejectEvent(r,e,RejectHandlerType.DISCARDOLDEST);
            if (!e.isShutdown()) {
                if (retrying.get() || e.getQueue().poll() == null) {
                    throw new RejectedExecutionException("Cannot discard oldest and enqueue task");
                }
                retrying.set(true);
                try { e.execute(r); } finally { retrying.remove(); }
            }
        }
    }

    /**
     * CallerRunsPolicy - 只用调用者的线程运行任务
     */
    private class CallerRunsPolicy implements RejectedExecutionHandler {
        public CallerRunsPolicy() {
        }
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            count.incrementAndGet();
            // 发布告警事件
            publishRejectEvent(r,e,RejectHandlerType.CALLERRUNS);
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    private void publishRejectEvent(Runnable r,ThreadPoolExecutor e,RejectHandlerType type){
        TPRejectPublisher.publishRejectEvent(TPRejectEvent.createEvent(r,getCount(),type.getRejectType(),e));
    }

    /**
     * 设置线程池信息
     */
    public void setPoolInfo(Long poolId, String poolName, RejectHandlerType
            type) {
        this.poolId = poolId;
        this.poolName = poolName;
    }
    /**
     * 获取拒绝次数
     */
    public long getCount() {
        return count.get();
    }

    /**
     * 重置计数
     */
    public  void resetCount() {
        count.set(0);
    }
}
