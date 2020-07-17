package com.quorum.tessera.key.vault.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;

public class AzureSecretClientFactoryTest {

    private AzureSecretClientFactory secretClientFactory;

    @Test
    public void createInvalidUrlThrowsException() {
        secretClientFactory = new AzureSecretClientFactory("noscheme", mock(TokenCredential.class));

        Throwable ex = catchThrowable(() -> secretClientFactory.create());

        assertThat(ex).isExactlyInstanceOf(IllegalArgumentException.class);
        assertThat(ex).hasMessage("The Azure Key Vault url is malformed.");
    }

    @Test
    public void create() {
        TokenCredential tokenCredential = mock(TokenCredential.class);

        secretClientFactory = new AzureSecretClientFactory("http://someurl", tokenCredential);
        SecretClient secretClient = secretClientFactory.create();

        assertThat(secretClient).isNotNull();
    }
}
