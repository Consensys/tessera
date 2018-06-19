package com.github.nexus.socket;

import java.util.concurrent.TimeUnit;
/*
Testable/mocable means of executing a Thread.sleep
*/
public interface ThreadDelegate {

    default void sleep(long millsecs) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(millsecs);
    }
    
    static ThreadDelegate create() {
        return new ThreadDelegate() {};
    }

}
