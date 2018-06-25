package com.github.nexus.socket;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

        verify(executorService).scheduleWithFixedDelay(any(Runnable.class), eq(1L), eq(1L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void executionThrowsError() throws InterruptedException {
        final CountDownLatch cdl = new CountDownLatch(1);

        final Runnable run = new Runnable() {
            private boolean hasRun = false;

            @Override
            public void run() {
                if(hasRun) {
                    cdl.countDown();
                } else {
                    hasRun = true;
                    throw new RuntimeException();
                }
            }
        };

        final SocketExecutor socketExecutor = new SocketExecutor(Executors.newSingleThreadScheduledExecutor(), run);
        socketExecutor.start();

        final boolean await = cdl.await(5, TimeUnit.SECONDS);
        assertThat(await).isTrue();
    }

    @Test
    public void stop() {
        socketExecutor.stop();
        verify(executorService).shutdown();
    }

}
