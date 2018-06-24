package com.github.nexus.socket;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SocketExecutorTest {

    private ScheduledExecutorService executorService;

    private SocketServer socketServer;

    private SocketExecutor socketExecutor;

    @Before
    public void init() {
        this.executorService = mock(ScheduledExecutorService.class);
        this.socketServer = mock(SocketServer.class);

        this.socketExecutor = new SocketExecutor(executorService, socketServer);
    }

    @Test
    public void start() {
        socketExecutor.start();

        verify(executorService).scheduleWithFixedDelay(socketServer, 1, 1, TimeUnit.MILLISECONDS);

    }

    @Test
    public void stop() {
        socketExecutor.stop();
        verify(executorService).shutdown();
    }

}
