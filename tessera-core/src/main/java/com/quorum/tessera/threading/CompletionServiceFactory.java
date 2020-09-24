package com.quorum.tessera.threading;

import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

public class CompletionServiceFactory {

    private final Executor executor = Executors.newCachedThreadPool();

    // TODO(cjh) make generic
    public CompletionService<Void> create() {
        return new ExecutorCompletionService<>(executor);
    }

}
