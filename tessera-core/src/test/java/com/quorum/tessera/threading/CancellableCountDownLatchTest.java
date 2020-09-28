package com.quorum.tessera.threading;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class CancellableCountDownLatchTest {

    private final int initCount = 10;

    private CancellableCountDownLatch countDownLatch;

    @Before
    public void onSetup() {
        countDownLatch = new CancellableCountDownLatch(initCount);
    }

    @Test
    public void constructor() {
        assertThat(countDownLatch).isNotNull();
        assertThat(countDownLatch.getCount()).isEqualTo(initCount);
    }

    @Test
    public void cancelWithException() {
        final RuntimeException ex = new RuntimeException("some exception");

        assertThat(countDownLatch.getCount()).isEqualTo(10);
        assertThat(countDownLatch.getException()).isEqualTo(null);
        assertThat(countDownLatch.isCancelled()).isEqualTo(false);

        countDownLatch.cancelWithException(ex);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
        assertThat(countDownLatch.getException()).isEqualTo(ex);
        assertThat(countDownLatch.isCancelled()).isEqualTo(true);
    }

    @Test
    public void cancelWithExceptionDoesNothingIfAlreadyCancelled() {
        final RuntimeException ex = new RuntimeException("some exception");
        final RuntimeException otherEx = new RuntimeException("some other exception");

        countDownLatch.cancelWithException(ex);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
        assertThat(countDownLatch.getException()).isEqualTo(ex);
        assertThat(countDownLatch.isCancelled()).isEqualTo(true);

        countDownLatch.cancelWithException(otherEx);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
        assertThat(countDownLatch.getException()).isEqualTo(ex);
        assertThat(countDownLatch.isCancelled()).isEqualTo(true);
    }

    @Test
    public void cancelWithExceptionDoesNothingIfLatchAlreadyOpen() {
        countDownLatch = new CancellableCountDownLatch(0);
        final RuntimeException ex = new RuntimeException("some exception");

        assertThat(countDownLatch.getCount()).isEqualTo(0);
        assertThat(countDownLatch.getException()).isEqualTo(null);
        assertThat(countDownLatch.isCancelled()).isEqualTo(false);

        countDownLatch.cancelWithException(ex);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
        assertThat(countDownLatch.getException()).isEqualTo(null);
        assertThat(countDownLatch.isCancelled()).isEqualTo(false);
    }

    @Test
    public void await() throws InterruptedException {
        final AtomicBoolean isThreadFinished = new AtomicBoolean();

        final Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(
                () -> {
                    while (countDownLatch.getCount() > 0) {
                        countDownLatch.countDown();
                    }
                    isThreadFinished.set(true);
                });

        countDownLatch.await();

        assertThat(isThreadFinished.get()).isTrue();
    }

    @Test
    public void awaitIfCancelledThrowsExceptionAndDoesNotWait() {
        final IllegalStateException cause = new IllegalStateException("some exception");

        countDownLatch.cancelWithException(cause);

        final Throwable ex = catchThrowable(() -> countDownLatch.await());

        assertThat(ex).isEqualTo(cause);
    }

    @Test
    public void awaitIfCancelledThrowsDefaultException() {
        countDownLatch.cancelWithException(null);

        final Throwable ex = catchThrowable(() -> countDownLatch.await());

        assertThat(ex).isExactlyInstanceOf(CountDownLatchCancelledException.class);
    }

    @Test
    public void awaitTimeout() throws InterruptedException {
        final AtomicBoolean isThreadFinished = new AtomicBoolean();

        final Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(
                () -> {
                    while (countDownLatch.getCount() > 0) {
                        countDownLatch.countDown();
                    }
                    isThreadFinished.set(true);
                });

        final boolean result = countDownLatch.await(100, TimeUnit.MILLISECONDS);

        assertThat(result).isTrue();
        assertThat(isThreadFinished.get()).isTrue();
    }

    @Test
    public void awaitTimeoutExceeded() throws InterruptedException {
        final Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(
                () -> {
                    Throwable ex = catchThrowable(() -> Thread.sleep(1000));
                    assertThat(ex).isNull();
                });

        final boolean result = countDownLatch.await(100, TimeUnit.MILLISECONDS);

        assertThat(result).isFalse();
    }

    @Test
    public void awaitTimeoutIfCancelledThrowsExceptionAndDoesNotWait() {
        final IllegalStateException cause = new IllegalStateException("some exception");

        countDownLatch.cancelWithException(cause);

        final Throwable ex = catchThrowable(() -> countDownLatch.await(1000, TimeUnit.SECONDS));

        assertThat(ex).isEqualTo(cause);
    }

    @Test
    public void awaitTimeoutIfCancelledThrowsDefaultException() {
        countDownLatch.cancelWithException(null);

        final Throwable ex = catchThrowable(() -> countDownLatch.await(1000, TimeUnit.SECONDS));

        assertThat(ex).isExactlyInstanceOf(CountDownLatchCancelledException.class);
    }
}
