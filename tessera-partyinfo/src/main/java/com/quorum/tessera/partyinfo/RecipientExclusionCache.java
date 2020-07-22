package com.quorum.tessera.partyinfo;

import com.quorum.tessera.partyinfo.model.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RecipientExclusionCache implements ExclusionCache<Recipient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipientExclusionCache.class);

    private final ConcurrentSkipListSet<ExpiryKey<Recipient>> excluded
        = new ConcurrentSkipListSet<>(Comparator.comparing(ExpiryKey::getExpiry));

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private long interval;

    private TimeUnit timeUnit;

    private static final AtomicLong EXPIRE = new AtomicLong();

    public RecipientExclusionCache() {
        this(10, 5, TimeUnit.SECONDS);
    }

    protected RecipientExclusionCache(long expiry, long interval, TimeUnit timeUnit) {
        this.interval = interval;
        this.timeUnit = timeUnit;
        EXPIRE.set(expiry);
    }

    @Override
    public boolean isExcluded(Recipient recipient) {
        return excluded.stream().map(ExpiryKey::getItem).anyMatch(r -> r.equals(recipient));
    }

    @Override
    public ExclusionCache<Recipient> exclude(Recipient recipient) {
        excluded.add(ExpiryKey.from(recipient));
        return this;
    }

    @Override
    public ExclusionCache<Recipient> start() {

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LOGGER.info("Check expired");
            LocalDateTime now = LocalDateTime.now();
            excluded.removeIf(k -> k.getExpiry().isAfter(now));

        }, interval, interval, timeUnit);

        return this;
    }

    @Override
    public void stop() {
        scheduledExecutorService.shutdown();
    }

    protected static class ExpiryKey<T> {

        private final LocalDateTime expiry;

        private final T item;

        private ExpiryKey(LocalDateTime expiry, T item) {
            this.expiry = expiry;
            this.item = item;
        }

        public LocalDateTime getExpiry() {
            return expiry;
        }

        public T getItem() {
            return item;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExpiryKey<?> expiryKey = (ExpiryKey<?>) o;
            return Objects.equals(item, expiryKey.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item);
        }

        private static <T> ExpiryKey<T> from(T item) {
            Objects.requireNonNull(item,"sItem is required");
            return new ExpiryKey(LocalDateTime.now()
                .plusSeconds(EXPIRE.get()), item);
        }
    }

}
