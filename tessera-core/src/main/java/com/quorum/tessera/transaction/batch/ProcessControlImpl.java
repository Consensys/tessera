package com.quorum.tessera.transaction.batch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessControlImpl implements ProcessControl {

    private ExecutorService executorService;

    private Runtime runtime;

    public ProcessControlImpl() {
        this(Executors.newSingleThreadExecutor(), Runtime.getRuntime());
    }

    public ProcessControlImpl(ExecutorService executorService, Runtime runtime) {
        this.executorService = executorService;
        this.runtime = runtime;
    }

    @Override
    public void exit(int code) {
        runtime.exit(code);
    }

    @Override
    public void start(Runnable toRun, Runnable shutdownHook) {
        runtime.addShutdownHook(new Thread(shutdownHook));

        executorService.submit(toRun);
    }
}
