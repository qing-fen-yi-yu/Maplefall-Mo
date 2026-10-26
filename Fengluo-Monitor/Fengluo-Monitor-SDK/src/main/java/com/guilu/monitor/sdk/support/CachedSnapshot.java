package com.guilu.monitor.sdk.support;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class CachedSnapshot<T> implements Supplier<T> {

    private static final System.Logger LOG = System.getLogger(CachedSnapshot.class.getName());

    private final Supplier<T> loader;
    private final long ttlNanos;
    private final AtomicReference<Entry<T>> current = new AtomicReference<>();

    public CachedSnapshot(Supplier<T> loader, Duration ttl) {
        this.loader = Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(ttl, "ttl");
        this.ttlNanos = ttl.toNanos();
        if (ttlNanos < 1L) {
            throw new IllegalArgumentException("cache ttl must be at least 1ms");
        }
    }

    @Override
    public T get() {
        Entry<T> entry = current.get();
        if (isFresh(entry, System.nanoTime())) {
            return entry.value;
        }
        synchronized (this) {
            entry = current.get();
            long now = System.nanoTime();
            if (isFresh(entry, now)) {
                return entry.value;
            }
            try {
                T value = loader.get();
                if (value != null) {
                    current.set(new Entry<>(value, now));
                    return value;
                }
            } catch (RuntimeException e) {
                LOG.log(System.Logger.Level.WARNING, "Snapshot loader failed; using last value", e);
            }
            return entry == null ? null : entry.value;
        }
    }

    public void invalidate() {
        current.set(null);
    }

    private boolean isFresh(Entry<T> entry, long now) {
        return entry != null && now - entry.loadedAt < ttlNanos;
    }

    private record Entry<T>(T value, long loadedAt) {
    }
}
