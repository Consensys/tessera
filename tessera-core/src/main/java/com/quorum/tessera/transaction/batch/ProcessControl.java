package com.quorum.tessera.transaction.batch;

public interface ProcessControl {
    int SUCCESS = 0;
    int FAILURE = 1;
    int PARTIAL_SUCCESS = 2;
    int STOPPED = 3;
    void exit(int code);
    void start(Runnable toRun, Runnable shutdownHook);
}
