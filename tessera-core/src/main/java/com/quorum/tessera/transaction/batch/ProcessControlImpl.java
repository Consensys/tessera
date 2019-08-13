package com.quorum.tessera.transaction.batch;

public class ProcessControlImpl implements ProcessControl{

    @Override
    public void exit(int code) {
        Runtime.getRuntime().exit(code);
    }

    @Override
    public void start(Runnable toRun, Runnable shutdownHook) {
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
        Thread thr = new Thread(toRun);
        thr.start();
    }
}
