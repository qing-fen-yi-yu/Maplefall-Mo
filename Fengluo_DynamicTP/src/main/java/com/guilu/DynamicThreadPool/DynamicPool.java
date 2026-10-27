package com.guilu.DynamicThreadPool;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import com.guilu.DynamicThreadPool.FLDynamicReject.DynamicRejectHandler;
import com.guilu.domain.po.ThreadPool.ThreadPoolConfig;
import com.guilu.enums.BlockingQueueType;
import com.guilu.enums.PoolStatus;
import com.guilu.enums.RejectHandlerType;
import com.guilu.exception.BusinessException.BadDataException;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.*;

public class DynamicPool extends ThreadPoolExecutor {
    private final Long threadId;
    private volatile String threadName;
    private volatile TimeUnit timeUnit;
    public DynamicPool(
            Long id,String threadName,
            int corePoolSize, int maximumPoolSize,
                             long keepAliveTime, TimeUnit unit,
                             BlockingQueue<Runnable> workQueue,
                             ThreadFactory threadFactory,
                             RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.threadId = id;
        this.threadName = threadName;
        this.timeUnit = unit;
    }
    public DynamicPool(ThreadPoolConfig threadPoolConfig, Long id){
        super(
                threadPoolConfig.getCoreSize()
                , threadPoolConfig.getMaxPoolSize(),
                threadPoolConfig.getKeepAliveTime(),
                creatTimeUnit(threadPoolConfig.getTimeUnit()),
                createBlockingType(threadPoolConfig.getBlockingQueueType(), threadPoolConfig.getBlockingQueueSize()),
                ThreadUtil.createThreadFactory(threadPoolConfig.getThreadFactory()),
                createRejectHandler(threadPoolConfig.getRejectHandler())
        );
        this.threadId = id;
        this.threadName = threadPoolConfig.getThreadPoolName();
        this.timeUnit = creatTimeUnit(threadPoolConfig.getTimeUnit());
    }

    private static BlockingQueue<Runnable> createBlockingType(String blockingQueueType, Integer blockingQueueSize) {
        try {
            BlockingQueue<Runnable> blockingQueue = BlockingQueueType.of(blockingQueueType, blockingQueueSize);
            if (blockingQueue == null) {
                throw new BadDataException("阻塞队列值错误");
            }
            return blockingQueue;
        } catch (IllegalArgumentException e) {
            throw new BadDataException("阻塞队列容量值错误: " + blockingQueueSize);
        }
    }

    private static TimeUnit creatTimeUnit(String timeUnit) {
        if (!StringUtils.hasText(timeUnit)) {
            throw new BadDataException("时间单位不能为空");
        }
        try {
            return TimeUnit.valueOf(timeUnit.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadDataException("时间单位值错误: " + timeUnit);
        }
    }

    private static RejectedExecutionHandler createRejectHandler(String rejectHandler) {
        RejectedExecutionHandler rejectedExecutionHandler = RejectHandlerType.of(rejectHandler);
        if(rejectedExecutionHandler == null){
            throw new BadDataException("拒绝策略值错误");
        }
        return rejectedExecutionHandler;
    }


    //获取线程池基础信息
    public int getCorePoolSize() {
        //获取核心线程数量
        return super.getCorePoolSize();
    }
    public int getMaximumPoolSize() {
        //获取最大线程数量
        return super.getMaximumPoolSize();
    }
    public long getKeepAliveTime() {
        //获取空闲进程最大存活时间
        return super.getKeepAliveTime(timeUnit);
    }
    public int getPoolSize(){
        return super.getPoolSize();
    }
    public String getRejectHandler() {
        RejectedExecutionHandler handler = super.getRejectedExecutionHandler();
        return handler instanceof DynamicRejectHandler dynamic
                ? dynamic.getType().name()
                : handler.getClass().getSimpleName(); //获取线程池执行策略
    }
    public ThreadFactory getThreadFactory() {
        return super.getThreadFactory(); //获取线程创建工厂
    }
    public int getQueueSize() {
        //获取阻塞队列大小
        return super.getQueue().size();
    }
    public String getQueueType() {
        return getQueue().getClass().getSimpleName();
    }
    public String getTimeUnit() {
        return timeUnit == null ? null : timeUnit.toString();
    }
    public String getThreadPoolName() {
        return this.threadName;
    }
    public Long getThreadId() {
        return this.threadId;
    }
    public long getTaskCount() {
        return super.getTaskCount(); //获取任务数量
    }
    public int getActiveCount() {
        return super.getActiveCount(); //获取工作线程数量
    }
    public int getLargestPoolSize() {
        return super.getLargestPoolSize(); //获取线程池最大线程工作数量
    }
    public int getQueueRemainingCapacity(){
        //获取队列剩余容量
        return super.getQueue().remainingCapacity();
    }
    public long getCompletedTaskCount() {
        return super.getCompletedTaskCount();
    }
    public Map<PoolStatus,Boolean> getPoolStatus(){
        boolean shutdown = isShutdown(); // 线程池是否不再接收新的任务
        boolean terminating = isTerminating(); //线程池是否正在关闭
        boolean terminated = isTerminated(); //线程池是否彻底关闭
        return Map.of(
                PoolStatus.SHUTDOWN,shutdown,
                PoolStatus.TERMINATING,terminating,
                PoolStatus.TERMINATED,terminated
        );
    }
    //设置线程池核心参数信息
    public void setCoreSize(int size){
        if (size < 0 ){
            throw new BadDataException("核心线程数必须大于0");
        }
        super.setCorePoolSize(size);//修改线程池核心进程数
    }
    public void setMaxPoolSize(int size){
        if (size <= 0 || size <getCorePoolSize()){
            throw new BadDataException("最大线程数违法");
        }
        super.setMaximumPoolSize(size);//最大进程数
    }
    public void setKeepAliveTime(long time, TimeUnit unit){
        if (time < 0){
            throw new BadDataException("Alive Time 设定违法");
        }
        if (ObjectUtil.isEmpty(unit)){
            unit = this.timeUnit;
        }
        super.setKeepAliveTime(time, unit);
        this.timeUnit = unit;
    }
    public void setKeepAliveTime(long time, String unit){
        if (time < 0){
            throw new BadDataException("Alive Time 设定违法");
        }
        TimeUnit timeUnit1 = this.timeUnit;
        if (!(StringUtils.isEmpty(unit))){
            timeUnit1 = creatTimeUnit(unit);
        }

        setKeepAliveTime(time, timeUnit1);
    }
    public void setRejectHandler(String  rejectHandler){
        super.setRejectedExecutionHandler(createRejectHandler(rejectHandler));
    }
    public void setThreadFactory(String threadFactory){
        super.setThreadFactory(ThreadUtil.createThreadFactory(threadFactory));
    }

    public void setThreadPoolName(String threadPoolName){
        if (StringUtils.isEmpty(threadPoolName)){
            return;
        }
        this.threadName = threadPoolName;
    }
    public void setQueueSize(int size){
        BlockingQueue<Runnable> queue = getQueue();

        if (queue instanceof CapacityBlockingQueue<Runnable>){
            ((CapacityBlockingQueue<Runnable>) queue).setCapacity(size);
            return;
        }
        throw new BadDataException("该队列不支持调整大小");
    }
}
