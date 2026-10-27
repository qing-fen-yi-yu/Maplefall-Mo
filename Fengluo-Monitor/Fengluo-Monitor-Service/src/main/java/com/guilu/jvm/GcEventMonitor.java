package com.guilu.jvm;
import com.guilu.monitor.domain.GcEventInfo;
import com.sun.management.GarbageCollectionNotificationInfo;
import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.util.*;
import java.util.function.Consumer;


/**
 * 垃圾回收事件监听器
 */
public final class GcEventMonitor implements AutoCloseable {
    private final Map<NotificationEmitter, NotificationListener> listeners = new LinkedHashMap<>();
    private final Map<String, GcEventInfo> latest = new LinkedHashMap<>();
    private final Consumer<GcEventInfo> consumer;
    private boolean closed;
    public GcEventMonitor() {
        this(event -> {});
    }
    public GcEventMonitor(Consumer<GcEventInfo> consumer) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        try {
            for (GarbageCollectorMXBean bean : GcMxBeanMetrics.getGcMxBeans()) {
                if (bean instanceof NotificationEmitter emitter) {
                    NotificationListener listener = (notification, handback) -> accept(notification);
                    emitter.addNotificationListener(listener, null, null);
                    listeners.put(emitter, listener);
                }
            }
        } catch (RuntimeException e) {
            close(); throw e;
        }
    }
    private void accept(Notification notification) {
        GcEventInfo event;
        synchronized (this) {
            if (closed || !GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION.equals(notification.getType())
                    || !(notification.getUserData() instanceof CompositeData data)) {
                return;
            }
            GarbageCollectionNotificationInfo notificationInfo = GarbageCollectionNotificationInfo.from(data);
            com.sun.management.GcInfo gc = notificationInfo.getGcInfo();
            event = new GcEventInfo(
                    notificationInfo.getGcName(), notificationInfo.getGcAction(),
                    notificationInfo.getGcCause(), gc.getId(), gc.getStartTime(), gc.getEndTime(),
                    gc.getDuration(), gc.getMemoryUsageBeforeGc(), gc.getMemoryUsageAfterGc());
            latest.put(event.collector(), event);
        }
        // 消费者在锁外执行，避免指标记录阻塞 GC 通知的其它监听器。
        try {
            consumer.accept(event);
        } catch (RuntimeException e) {
            System.getLogger(GcEventMonitor.class.getName())
                    .log(System.Logger.Level.WARNING, "GC consumer failed", e);
        }
    }
    public synchronized Map<String, GcEventInfo> getLatestEvents() {
        return Map.copyOf(latest);
    }
    public synchronized int getSubscriptionCount() {
        return listeners.size();
    }
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        listeners.forEach((emitter, listener) -> {
            try { emitter.removeNotificationListener(listener); }
            catch (ListenerNotFoundException ignored) { /* Already removed. */ }
        });
        listeners.clear();
    }
}
