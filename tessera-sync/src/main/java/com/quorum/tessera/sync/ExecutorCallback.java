package com.quorum.tessera.sync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface ExecutorCallback<T> {

    T doExecute() throws InterruptedException, ExecutionException, TimeoutException;

    static <T> T execute(ExecutorCallback<T> callback) {
        try {
            return callback.doExecute();
        } catch (TimeoutException ex) {
            throw new UncheckedTimeoutException(ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
