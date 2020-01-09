package com.quorum.tessera.key.vault;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NoKeyVaultServiceFactoryExceptionTest {

    @Test
    public void createWithMessage() {
        final String msg = "msg";
        VaultSecretNotFoundException exception = new VaultSecretNotFoundException(msg);

        assertThat(exception).hasMessage(msg);
    }
}
