package com.guilu.enums;

import com.guilu.DynamicThreadPool.FLBlockingQueue.CapacityBlockingQueue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.SynchronousQueue;

@AllArgsConstructor
@Getter
public enum BlockingQueueType {
    LINKEDBLOCKINGQUEUE("LinkedBlocking"),
    SYNCHRONOUSQUEUE("Synchronous"),
    LINKEDTRANSFERQUEUE("LinkedTransfer"),
    ARRAYBLOCKINGQUEUE("ArrayBlocking"),
    CAPACITYBLOCKINGQUEUE("CapacityBlocking");

    private final String queueName;

    /**
     * Resolves a queue type by enum name ({@code ARRAYBLOCKINGQUEUE}) or by
     * configured name ({@code ArrayBlocking}), case-insensitively.
     */
    public static BlockingQueueType from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        for (BlockingQueueType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)
                    || type.queueName.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return null;
    }

    public static BlockingQueue<Runnable> of(String queueName, Integer blockingQueueSize) {
        BlockingQueueType type = from(queueName);
        if (type == null) {
            return null;
        }
        int capacity = blockingQueueSize == null ? -1 : blockingQueueSize;
        return switch (type) {
            // A non-positive capacity keeps the historical unbounded behaviour.
            case LINKEDBLOCKINGQUEUE -> capacity > 0
                    ? new LinkedBlockingQueue<>(capacity)
                    : new LinkedBlockingQueue<>();
            case SYNCHRONOUSQUEUE -> new SynchronousQueue<>();
            case LINKEDTRANSFERQUEUE -> new LinkedTransferQueue<>();
            case ARRAYBLOCKINGQUEUE -> new ArrayBlockingQueue<>(requirePositive(type, capacity));
            case CAPACITYBLOCKINGQUEUE -> new CapacityBlockingQueue<>(requirePositive(type, capacity));
        };
    }

    private static int requirePositive(BlockingQueueType type, int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(type.queueName + " requires a positive capacity");
        }
        return capacity;
    }
}
