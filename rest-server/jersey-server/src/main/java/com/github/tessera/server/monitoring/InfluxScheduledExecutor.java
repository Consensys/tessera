package com.github.tessera.server.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

public class InfluxScheduledExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxScheduledExecutor.class);

    private final ScheduledExecutorService executor;
    private final Runnable runnable;
    private final long delayInSeconds;

    public InfluxScheduledExecutor() {
        executor = newSingleThreadScheduledExecutor();
        InfluxDbClient client = new InfluxDbClient();
        runnable = new InfluxPublisher(client);
        delayInSeconds = 5; //TODO define in config file
    }

    public void start() {
        final Runnable exceptionSafeRunnable = () -> {
            try {
                runnable.run();
            } catch (final Throwable ex) {
                LOGGER.error("Error when executing action {}", runnable.getClass().getSimpleName());
                LOGGER.error("Error when executing action", ex);
            }
        };

        this.executor.scheduleWithFixedDelay(exceptionSafeRunnable, delayInSeconds, delayInSeconds, TimeUnit.SECONDS);
    }
}
