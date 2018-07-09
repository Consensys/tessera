package com.github.tessera.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SocketExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketExecutor.class);

    private final ScheduledExecutorService executor;

    private final Runnable socketServer;

    public SocketExecutor(final ScheduledExecutorService executor, final Runnable socketServer) {
        this.executor = Objects.requireNonNull(executor);
        this.socketServer = Objects.requireNonNull(socketServer);
    }

    @PostConstruct
    public void start() {
        final Runnable exceptionSafeRunnable = () -> {
            try {
                socketServer.run();
            } catch (final Throwable ex) {
                LOGGER.error("Error when executing socket listener", ex);
            }
        };

        this.executor.scheduleWithFixedDelay(exceptionSafeRunnable, 1L, 1L, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }

}
