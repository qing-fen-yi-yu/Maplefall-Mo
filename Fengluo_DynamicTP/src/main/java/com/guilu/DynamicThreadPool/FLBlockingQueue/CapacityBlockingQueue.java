package com.guilu.DynamicThreadPool.FLBlockingQueue;

import com.guilu.exception.BusinessException.BadDataException;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//todo 实现对各个监控队列的任务耗时，队列长度进行监控
public class CapacityBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {
    private static final class Entry<T> {
        final T value;
        Entry(T value) { this.value = value; }
    }
    private final Deque<Entry<E>> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();
    private int capacity;
    public CapacityBlockingQueue(int capacity) {
        if (capacity <= 0) throw new BadDataException("Capacity值不合法");
        this.capacity = capacity;
    }
    public int getCapacity() {
        lock.lock();
        try {
            return capacity;
        } finally {
            lock.unlock();
        }
    }
    public void setCapacity(int capacity) {
        if (capacity <= 0) throw new BadDataException("Capacity值不合法");
        lock.lock();
        try {
            this.capacity = capacity;
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }
    private void enqueue(E e) {
        queue.addLast(new Entry<>(e));
        notEmpty.signal();
    }
    private E dequeue() {
        E e = queue.removeFirst().value;
        notFull.signalAll();
        return e;
    }
    @Override
    public boolean offer(E e) {
        Objects.requireNonNull(e);
        lock.lock();
        try {
            if (queue.size() >= capacity) {
                return false;
            }
            enqueue(e);
            return true;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public void put(E e) throws InterruptedException {
        Objects.requireNonNull(e);
        lock.lockInterruptibly();
        try {
            while (queue.size() >= capacity) {
                notFull.await();
            }
            enqueue(e);
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e);
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (queue.size() >= capacity) {
                if (nanos <= 0) return false;
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(e); return true;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public E poll() {
        lock.lock();
        try {
            return queue.isEmpty() ? null : dequeue();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public E take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (queue.isEmpty()) notEmpty.await();
            return dequeue();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        lock.lockInterruptibly();
        try {
            while (queue.isEmpty()) {
                if (nanos <= 0) return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            return dequeue();
        } finally { lock.unlock(); }
    }
    @Override
    public E peek() {
        lock.lock();
        try {
            return queue.isEmpty() ? null : queue.getFirst().value;
        } finally {
            lock.unlock();
        }
    }
    @Override
    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public int remainingCapacity() {
        lock.lock();
        try {
            return Math.max(0, capacity - queue.size());
        } finally {
            lock.unlock();
        }
    }
    @Override
    public boolean remove(Object value) {
        lock.lock();
        try {
            var iterator = queue.iterator();
            while (iterator.hasNext()) {
                if (Objects.equals(iterator.next().value, value)) {
                    iterator.remove(); notFull.signalAll(); return true;
                }
            }
            return false;
        } finally { lock.unlock(); }
    }
    @Override
    public boolean contains(Object value) {
        lock.lock();
        try { return queue.stream().anyMatch(e -> Objects.equals(e.value, value)); }
        finally { lock.unlock(); }
    }
    @Override
    public void clear() {
        lock.lock();
        try {
            queue.clear();
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }
    @Override
    public int drainTo(Collection<? super E> target) {
        return drainTo(target, Integer.MAX_VALUE);
    }
    @Override
    public int drainTo(Collection<? super E> target, int maxElements) {
        Objects.requireNonNull(target);
        if (target == this) throw new IllegalArgumentException("Cannot drain to self");
        lock.lock();
        try {
            int count = 0;
            while (count < maxElements && !queue.isEmpty()) {
                target.add(queue.getFirst().value);
                dequeue(); count++;
            }
            return count;
        } finally { lock.unlock(); }
    }
    @Override
    public Iterator<E> iterator() {
        List<Entry<E>> snapshot;
        lock.lock();
        try {
            snapshot = new ArrayList<>(queue);
        } finally {
            lock.unlock();
        }
        return new Iterator<>() {
            private final Iterator<Entry<E>> iterator = snapshot.iterator();
            private Entry<E> current;
            public boolean hasNext() { return iterator.hasNext(); }
            public E next() { current = iterator.next(); return current.value; }
            public void remove() {
                if (current == null) throw new IllegalStateException();
                lock.lock();
                try { if (queue.remove(current)) notFull.signalAll(); }
                finally { lock.unlock(); current = null; }
            }
        };
    }
}
