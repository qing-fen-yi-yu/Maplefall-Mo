package com.guilu.threadPool;

import com.guilu.exception.BusinessException.ThreadPoolException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("all")
@Slf4j
public class CapacityBlockingQueue<E> implements BlockingQueue<E> {
    private final LinkedBlockingQueue<E> queue = new LinkedBlockingQueue<>();
    private volatile int capacity;//队列大小
    private final AtomicInteger size = new AtomicInteger(0); //队列任务数量
    private final ReentrantLock resizeLock = new ReentrantLock();

    public CapacityBlockingQueue(int initialCapacity) {
        if (initialCapacity <= 0) throw new ThreadPoolException("容量值违法");
        this.capacity = initialCapacity;
    }

    public void setCapacity(int newCapacity) {
        if (newCapacity <= 0) throw new ThreadPoolException("容量值违法");
        resizeLock.lock();
        try {
            int oldCapacity = this.capacity;
            this.capacity = newCapacity;
            if (newCapacity < oldCapacity) {
                while (size.get() > newCapacity) {
                    E removed = queue.poll();
                    if (removed != null) {
                        size.decrementAndGet();
                        log.warn("缩容丢弃任务: {}", removed);
                    } else {
                        break;
                    }
                }
            }
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public int remainingCapacity() {
        //返回队列剩余容量
        return capacity - size.get();
    }

    @Override
    public int size() {
        //返回队列大小
        return size.get();
    }

    //入队方法
    @Override
    public boolean add(E e) {
        if (size.get() >= capacity) {
            throw new IllegalStateException("Queue full");
        }
        size.incrementAndGet();   // 先占位
        queue.add(e);             // 底层无界，不会失败
        return true;
    }

    @Override
    public boolean offer(E e) {
        if (size.get() >= capacity) {
            return false;
        }
        size.incrementAndGet();
        queue.offer(e);
        return true;
    }

    @Override
    public void put(E e) throws InterruptedException {
        boolean flag = true;
        Long current = System.currentTimeMillis();
        // 自旋等待直到有容量,最大等待时间为5s
        while (System.currentTimeMillis() - current < 5000) {
            if (size.get() >= capacity){
                Thread.sleep(500); //避免一直循环造成系统资源耗尽
            }else {
                flag = false;
                size.incrementAndGet();
                queue.put(e);
                break;
            }
        }
        if (flag) {
            log.error("等待超时:{}",e);
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        long deadline = System.nanoTime() + nanos;
        while (size.get() >= capacity) {
            if (nanos <= 0) return false;
            Thread.sleep(1); // 同样可优化为 Condition.awaitNanos
            nanos = deadline - System.nanoTime();
        }
        size.incrementAndGet();
        return queue.offer(e); // 无界，直接成功
    }

    @Override
    public E poll() {
        E e = queue.poll();
        if (e != null) size.decrementAndGet();
        return e;
    }

    @Override
    public E take() throws InterruptedException {
        E e = queue.take();
        size.decrementAndGet();
        return e;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = queue.poll(timeout, unit);
        if (e != null) size.decrementAndGet();
        return e;
    }

    @Override
    public E remove() {
        // 队列空时抛出异常，由底层实现
        E e = queue.remove(); // 空时抛 NoSuchElementException
        size.decrementAndGet();
        return e;
    }

    @Override
    public boolean remove(Object o) {
        resizeLock.lock();
        try {
            boolean removed = queue.remove(o);
            if (removed) size.decrementAndGet();
            return removed;
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public void clear() {
        resizeLock.lock();
        try {
            queue.clear();
            size.set(0);
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        resizeLock.lock();
        try {
            int n = queue.drainTo(c);
            size.addAndGet(-n);
            return n;
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        resizeLock.lock();
        try {
            int n = queue.drainTo(c, maxElements);
            size.addAndGet(-n);
            return n;
        } finally {
            resizeLock.unlock();
        }
    }
    @Override public E element() { return queue.element(); }
    @Override public E peek() { return queue.peek(); }
    @Override public boolean isEmpty() { return queue.isEmpty(); }
    @Override public boolean contains(Object o) { return queue.contains(o); }
    @Override public Iterator<E> iterator() { return queue.iterator(); }
    @Override public Object[] toArray() { return queue.toArray(); }
    @Override public Object[] toArray(Object[] a) { return queue.toArray(a); }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        resizeLock.lock();
        try {
            for (E e : c) {
                add(e);  // 内部会检查容量并增加 size
            }
            return true;
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        resizeLock.lock();
        try {
            int before = size.get();
            boolean changed = queue.removeAll(c);
            int after = queue.size();
            size.set(after);   // 重新同步 size
            return changed;
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        resizeLock.lock();
        try {
            boolean changed = queue.retainAll(c);
            size.set(queue.size()); // 同步
            return changed;
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CapacityBlockingQueue)) return false;
        CapacityBlockingQueue<?> that = (CapacityBlockingQueue<?>) o;
        return queue.equals(that.queue);
    }

    @Override
    public int hashCode() {
        return queue.hashCode();
    }
}