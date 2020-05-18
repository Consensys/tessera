package com.quorum.tessera.recover;

public interface Recovery {

    RecoveryResult request();

    RecoveryResult stage();

    RecoveryResult sync();
}
