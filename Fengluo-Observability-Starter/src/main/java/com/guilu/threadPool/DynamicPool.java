package com.guilu.threadPool;

import cn.hutool.core.util.ObjectUtil;
import com.guilu.domain.po.ThreadPool.TaskPoolConfig;
import com.guilu.exception.BusinessException.ThreadPoolException;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.*;

import static cn.hutool.core.thread.ThreadUtil.createThreadFactory;

public class DynamicPool extends ThreadPoolExecutor {
    private final Long threadId;
    private String threadName;
    private TimeUnit timeUnit;
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
    public DynamicPool(TaskPoolConfig taskPoolConfig, Long id){
        super(
                taskPoolConfig.getCoreSize()
                ,taskPoolConfig.getMaxPoolSize(),
                taskPoolConfig.getKeepAliveTime(),
                creatTimeUnit(taskPoolConfig.getTimeUnit()),
                createBlockingType(taskPoolConfig.getBlockingQueueType(),taskPoolConfig.getBlockingQueueSize()),
                createThreadFactory(taskPoolConfig.getThreadFactory()),
                createRejectHandler(taskPoolConfig.getRejectHandler())
        );
        this.threadId = id;
        this.threadName = taskPoolConfig.getThreadPoolName();
        this.timeUnit = creatTimeUnit(taskPoolConfig.getTimeUnit());
    }

    private static BlockingQueue<Runnable> createBlockingType(String blockingQueueType, Integer blockingQueueSize) {
        return
            switch (blockingQueueType){
            case "LinkedBlockingQueue" -> new LinkedBlockingQueue<>(blockingQueueSize); //链表结构阻塞队列，吞吐量较高
            case "SynchronousQueue" -> new SynchronousQueue<>(); //不进行存储元素阻塞队列
            case "LinkedTransferQueue" -> new LinkedTransferQueue<>(); //创建无限的阻塞队列
            case "ArrayBlockingQueue" -> new ArrayBlockingQueue<>(blockingQueueSize); //默认有界阻塞队列
            default -> new CapacityBlockingQueue<>(blockingQueueSize);
            };
    }

    private static TimeUnit creatTimeUnit(String timeUnit) {
        return TimeUnit.valueOf(timeUnit);
    }

    private static RejectedExecutionHandler createRejectHandler(String rejectHandler) {
        return
            switch (rejectHandler) {
                case "CallerRunsPolicy" -> //只用调用者的线程运行任务
                        new DynamicRejectHandler().new CallerRunsPolicy();
                case "DiscardOldestPolicy" -> //丢弃队列中的任务，执行当前任务
                        new DynamicRejectHandler().new DiscardOldestPolicy();
                case "DiscardPolicy" -> //不进行处理拒绝的任务
                        new DynamicRejectHandler().new DiscardPolicy();
                case "AbortPolicy" -> //默认直接抛出异常
                        new DynamicRejectHandler().new AbortPolicy();
                default -> new DynamicRejectHandler();
            };
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
    public RejectedExecutionHandler getRejectHandler() {
        return super.getRejectedExecutionHandler(); //获取线程池执行策略
    }
    public ThreadFactory getThreadFactory() {
        return super.getThreadFactory(); //获取线程创建工厂
    }
    public int getQueueSize() {
        //获取阻塞队列大小
        return super.getQueue().size();
    }
    public String getQueueType() {
        String string = super.getQueue().toString();
        int index = string.lastIndexOf('.');
        int end =  string.lastIndexOf('@');
        if (index == -1 || end == -1) {
            return string;
        }
        return string.substring(index,end);
    }
    public String getTimeUnit() {
        return timeUnit.toString();
    }
    public String getThreadPoolName() {
        return this.threadName;
    }
    public long getThreadId() {
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
    public long getComplatedTaskCount() {
        return super.getCompletedTaskCount();
    }
    public Map<String,Integer> getPoolStatus(){
        //返回线程池状态 0:是，1:否
        int shutdown = isShutdown()?0:1; // 线程池是否不再接收新的任务
        int terminating = isTerminating()?0:1; //线程池是否正在关闭
        int terminated = isTerminated()? 0:1; //线程池是否彻底关闭
        return Map.of(
                "shutdown",shutdown,
                "terminating",terminating,
                "terminated",terminated
        );
    }
    //设置线程池核心参数信息
    public void setCoreSize(int size){
        if (size <= 0 ){
            throw new IllegalArgumentException("核心吸纳成熟必须大于0");
        }
        super.setCorePoolSize(size);//修改线程池核心进程数
    }
    public void setMaxPoolSize(int size){
        if (size <= 0 || size <getCorePoolSize()){
            throw new IllegalArgumentException("最大线程数违法");
        }
        super.setMaximumPoolSize(size);//最大进程数
    }
    public void setKeepAliveTime(long time, TimeUnit unit){
        if (time < 0){
            throw new ThreadPoolException("Alive Time 设定违法");
        }
        if (ObjectUtil.isEmpty(unit)){
            unit = this.timeUnit;
        }
        super.setKeepAliveTime(time, unit);
        this.timeUnit = unit;
    }
    public void setKeepAliveTime(long time, String unit){
        if (time < 0){
            throw new ThreadPoolException("Alive Time 设定违法");
        }
        TimeUnit timeUnit1 = this.timeUnit;
        if (!(StringUtils.isEmpty(unit))){
            timeUnit1 = creatTimeUnit(unit);
        }

        super.setKeepAliveTime(time, timeUnit1);
    }
    public void setRejectHandler(String  rejectHandler){
        super.setRejectedExecutionHandler(createRejectHandler(rejectHandler));
    }
    public void setThreadFactory(String threadFactory){
        super.setThreadFactory(createThreadFactory(threadFactory));
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
        throw new ThreadPoolException("该队列不支持调整大小");
    }
}
