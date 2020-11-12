package com.quorum.tessera.recovery;

import java.util.ServiceLoader;

public interface Recovery {

    int recover();

    RecoveryResult request();

    RecoveryResult stage();

    RecoveryResult sync();

    static Recovery create() {
        return ServiceLoader.load(Recovery.class).findFirst().get();
    }

}
