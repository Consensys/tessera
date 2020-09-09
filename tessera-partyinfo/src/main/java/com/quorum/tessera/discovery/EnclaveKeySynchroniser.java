package com.quorum.tessera.discovery;

import java.util.ServiceLoader;

public interface EnclaveKeySynchroniser extends Runnable {

    void syncKeys();

    default void run() {
        syncKeys();
    }

    static EnclaveKeySynchroniser getInstance() {
        return ServiceLoader.load(EnclaveKeySynchroniser.class).findFirst().get();
    }
}
