package com.guilu.enums;

import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.*;

@AllArgsConstructor
@Getter
public enum BlockingQueueType {
    LINKEDBLOCKINGQUEUE("LinkedBlocking"),
    SYNCHRONOUSQUEUE("Synchronous"),
    LINKEDTRANSFERQUEUE("LinkedTransfer"),
    ARRAYBLOCKINGQUEUE("ArrayBlocking"),
    CAPACITYBLOCKINGQUEUE("CapacityBlocking");
    private String queueName;
    public static BlockingQueue<Runnable> of(String queueName,Integer blockingQueueSize){
        if(queueName.equals(LINKEDBLOCKINGQUEUE.name())){
            new LinkedBlockingQueue<>(blockingQueueSize); //链表结构阻塞队列，吞吐量较高
        } else if(queueName.equals(SYNCHRONOUSQUEUE.name())){
            new SynchronousQueue<>(); //不进行存储元素阻塞队列
        } else if(queueName.equals(LINKEDTRANSFERQUEUE.name())){
            new LinkedTransferQueue<>(); //创建无限的阻塞队列
        } else if(queueName.equals(ARRAYBLOCKINGQUEUE.name())){
            new ArrayBlockingQueue<>(blockingQueueSize); //默认有界阻塞队列
        } else if(queueName.equals(CAPACITYBLOCKINGQUEUE.name())){
            new CapacityBlockingQueue<>(blockingQueueSize);
        }
        return null;
    }
}
