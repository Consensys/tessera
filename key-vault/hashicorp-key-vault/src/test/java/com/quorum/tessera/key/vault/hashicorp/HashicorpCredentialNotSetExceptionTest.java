package com.quorum.tessera.key.vault.hashicorp;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HashicorpCredentialNotSetExceptionTest {

    @Test
    public void createWithMessage() {
        final String msg = "msg";
        HashicorpCredentialNotSetException exception = new HashicorpCredentialNotSetException(msg);

        assertThat(exception).hasMessage(msg);
    }
}
