package com.quorum.tessera.discovery;

public interface EnclaveKeySynchroniser extends Runnable {

    void syncKeys();

    default void run() {
        syncKeys();
    }

}
