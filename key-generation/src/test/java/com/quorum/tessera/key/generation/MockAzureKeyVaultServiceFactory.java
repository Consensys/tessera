package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.vault.data.AzureGetSecretData;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockAzureKeyVaultServiceFactory implements KeyVaultServiceFactory {
    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
        KeyVaultService mock = mock(KeyVaultService.class);

        when(mock.getSecret(any(AzureGetSecretData.class))).thenReturn("publicSecret").thenReturn("privSecret");

        return mock;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.AZURE;
    }
}
