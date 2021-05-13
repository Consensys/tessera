package com.quorum.tessera.threading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class TesseraScheduledExecutorTest {

  private static final long RATE = 2000L;

  private ScheduledExecutorService executorService;

  private Runnable action;

  private TesseraScheduledExecutor tesseraScheduledExecutor;

  @Before
  public void init() {
    this.executorService = mock(ScheduledExecutorService.class);
    this.action = mock(Runnable.class);

    this.tesseraScheduledExecutor =
        new TesseraScheduledExecutor(executorService, action, RATE, RATE);
  }

  @Test
  public void start() {
    tesseraScheduledExecutor.start();

    verify(executorService)
        .scheduleWithFixedDelay(
            any(Runnable.class), anyLong(), anyLong(), eq(TimeUnit.MILLISECONDS));
  }

  @Test
  public void executionThrowsError() throws InterruptedException {
    final CountDownLatch cdl = new CountDownLatch(1);

    final Runnable runnable =
        new Runnable() {
          private boolean hasRun = false;

          @Override
          public void run() {
            if (hasRun) {
              cdl.countDown();
            } else {
              hasRun = true;
              throw new RuntimeException();
            }
          }
        };

    final TesseraScheduledExecutor executor =
        new TesseraScheduledExecutor(Executors.newSingleThreadScheduledExecutor(), runnable, 2, 2);

    executor.start();

    final boolean await = cdl.await(5, TimeUnit.SECONDS);
    assertThat(await).isTrue();
  }

  @Test
  public void stop() {
    tesseraScheduledExecutor.stop();
    verify(executorService).shutdown();
  }
}
