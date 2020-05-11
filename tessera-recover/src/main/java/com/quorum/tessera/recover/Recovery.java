package com.quorum.tessera.recover;

public interface Recovery {

    RecoveryResult requestResend();

    RecoveryResult stage();

    RecoveryResult sync();
}
