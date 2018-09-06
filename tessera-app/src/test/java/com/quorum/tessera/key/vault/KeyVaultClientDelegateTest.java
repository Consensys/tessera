package com.quorum.tessera.key.vault;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class KeyVaultClientDelegateTest {
    @Test
    public void getSecretCallsClient() {

        KeyVaultClientDelegate keyVaultClientDelegate = new KeyVaultClientDelegate(KeyVaultAuthenticator.getAuthenticatedClient());

        final Throwable vaultEx = catchThrowable(() -> keyVaultClientDelegate.getSecret("url", "secret"));

        assertThat(vaultEx).isInstanceOf(RuntimeException.class).hasMessage("java.net.UnknownHostException: url: nodename nor servname provided, or not known");
    }
}
