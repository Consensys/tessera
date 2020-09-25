package com.quorum.tessera.threading;

import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

public class CompletionServiceFactory<T> {

    public CompletionService<T> create(Executor executor) {
        return new ExecutorCompletionService<>(executor);
    }
}
