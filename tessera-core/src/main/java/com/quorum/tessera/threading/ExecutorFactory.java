package com.quorum.tessera.threading;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorFactory {

    public Executor create() {
        return Executors.newCachedThreadPool();
    }

}
