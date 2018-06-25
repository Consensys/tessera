package com.github.nexus.socket;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SocketExecutor {

    private final ScheduledExecutorService executor;

    private final SocketServer socketServer;

    public SocketExecutor(final ScheduledExecutorService executor, final SocketServer socketServer) {
        this.executor = Objects.requireNonNull(executor);
        this.socketServer = Objects.requireNonNull(socketServer);
    }

    @PostConstruct
    public void start() {
        this.executor.scheduleWithFixedDelay(socketServer, 1, 1, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void stop() {
        executor.shutdown();
    }

}
