package com.guilu.monitor.sdk.binder.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.guilu.constants.Constant;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 扩展日志处理附加器。
 * 所有队列均采用留新删旧策略
 */
public class ExpandAppender extends UnsynchronizedAppenderBase<ILoggingEvent>
        implements InitializingBean, DisposableBean {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private static final int LEVEL_QUEUE_COUNT = 5;

    private Logger rootLogger;

    @Getter //总日志记录队列的大小
    private final int queueCapacity;
    @Getter //appender执行数
    private final AtomicLong receivedCount = new AtomicLong();
    @Getter //总入队数量
    private final AtomicLong totalOfferedCount = new AtomicLong();
    @Getter //总出队数量
    private final AtomicLong totalEvictedCount = new AtomicLong();
    @Getter
    private final BlockingQueue<LogBackSnapshot> totalLogQueue;
    @Getter
    private final BlockingQueue<LogBackSnapshot> infoQueue;
    @Getter
    private final BlockingQueue<LogBackSnapshot> errorQueue;
    @Getter
    private final BlockingQueue<LogBackSnapshot> warnQueue;
    @Getter
    private final BlockingQueue<LogBackSnapshot> debugQueue;
    @Getter
    private final BlockingQueue<LogBackSnapshot> traceQueue;
    @Getter
    private final Map<String, AtomicLong> evictedByLevel = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext loggerContext)) {
            addWarn("ExpandAppender 未注册：ILoggerFactory 不是 LoggerContext");
            return;
        }
        setContext(loggerContext);
        start();
        this.rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        this.rootLogger.addAppender(this);
    }

    @Override
    protected void append(ILoggingEvent e) {
        try {
            Level level = e.getLevel();
            BlockingQueue<LogBackSnapshot> target;
            String levelName;
            if (Level.INFO.equals(level)) {
                target = infoQueue;
                levelName = "info";
            } else if (Level.ERROR.equals(level)) {
                target = errorQueue;
                levelName = "error";
            } else if (Level.WARN.equals(level)) {
                target = warnQueue;
                levelName = "warn";
            } else if (Level.DEBUG.equals(level)) {
                target = debugQueue;
                levelName = "debug";
            } else if (Level.TRACE.equals(level)) {
                target = traceQueue;
                levelName = "trace";
            } else {
                return;
            }

            receivedCount.incrementAndGet();

            Map<String, String> mdc = e.getMDCPropertyMap();
            String traceId = (mdc == null) ? null : mdc.get(Constant.REQUEST_ID_HEADER);
            String msg = e.getFormattedMessage();

            LogBackSnapshot snapshot = LogBackSnapshot.builder()
                    .level(level.toString())
                    .loggerName(e.getLoggerName())
                    .formattedMessage(msg)
                    .time(FORMATTER.format(Instant.ofEpochMilli(e.getTimeStamp())))
                    .traceId(traceId == null || traceId.isBlank() ? "Nan" : traceId)
                    .build();

            if (offerEvictOldest(totalLogQueue, snapshot)) {
                totalOfferedCount.incrementAndGet();
            } else {
                totalEvictedCount.incrementAndGet();
            }
            if (!offerEvictOldest(target, snapshot)) {
                evictedByLevel.computeIfAbsent(levelName, k -> new AtomicLong()).incrementAndGet();
            }
        } catch (Exception ex) {
            addError("ExpandAppender append failed", ex);
        }
    }

    @Override
    public void destroy() {
        if (rootLogger != null) {
            rootLogger.detachAppender(this);
        }
        stop();
        totalLogQueue.clear();
        infoQueue.clear();
        errorQueue.clear();
        warnQueue.clear();
        debugQueue.clear();
        traceQueue.clear();
    }

    public ExpandAppender(int capacity) {
        if (capacity < LEVEL_QUEUE_COUNT) {
            throw new IllegalArgumentException(
                    "capacity must be >= " + LEVEL_QUEUE_COUNT + ", got " + capacity);
        }
        this.queueCapacity = capacity;

        this.totalLogQueue = new ArrayBlockingQueue<>(capacity);

        int base = capacity / LEVEL_QUEUE_COUNT;
        int rem = capacity % LEVEL_QUEUE_COUNT;
        this.infoQueue  = new ArrayBlockingQueue<>(base + (rem-- > 0 ? 1 : 0));
        this.errorQueue = new ArrayBlockingQueue<>(base + (rem-- > 0 ? 1 : 0));
        this.warnQueue  = new ArrayBlockingQueue<>(base + (rem-- > 0 ? 1 : 0));
        this.debugQueue = new ArrayBlockingQueue<>(base + (rem-- > 0 ? 1 : 0));
        this.traceQueue = new ArrayBlockingQueue<>(base + (rem-- > 0 ? 1 : 0));
    }

    /**
     * 队列满时淘汰最旧元素后再入队，永远保留最新数据
     */
    private static boolean offerEvictOldest(BlockingQueue<LogBackSnapshot> q, LogBackSnapshot s) {
        if (q.offer(s)) {
            return true;
        }
        synchronized (q) {
            if (q.offer(s)) {
                return true;
            }
            q.poll();
            q.offer(s);
            return false;
        }
    }

    @Builder
    @Getter
    public static class LogBackSnapshot {
        private String level;
        private String loggerName;
        private String formattedMessage;
        private String time;
        private String traceId;

        @Override
        public String toString() {
            return "LogBackSnapshot{" +
                    "level='" + level + '\'' +
                    ", loggerName='" + loggerName + '\'' +
                    ", formattedMessage='" + formattedMessage + '\'' +
                    ", time='" + time + '\'' +
                    ", traceId='" + traceId + '\'' +
                    '}';
        }
    }
}