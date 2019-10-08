package com.quorum.tessera.sync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

public class ExecutorCallbackTest {

    @Test(expected = RuntimeException.class)
    public void throwInterruptedException() {
        ExecutorCallback.execute(() -> {
            throw new InterruptedException();
        });

    }

    @Test(expected = RuntimeException.class)
    public void throwTimeoutException() {
        ExecutorCallback.execute(() -> {
            throw new TimeoutException();
        });

    }

    @Test(expected = RuntimeException.class)
    public void throwExecutionException() {
        Throwable cause = new Exception("");
        ExecutorCallback.execute(() -> {
            throw new ExecutionException(cause);
        });

    }
}
