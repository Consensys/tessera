package net.consensys.tessera.migration.data;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {

    private final AtomicInteger counter = new AtomicInteger(0);

    private String prefix;

    public CustomThreadFactory(String prefix) {
        this.prefix = Objects.requireNonNull(prefix);
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = String.format("%s-%d",prefix,counter.incrementAndGet());
        return new Thread(r,name);
    }
}
