package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyVaultType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultClientFactory;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import static org.mockito.Mockito.mock;

public class MockAzureKeyVaultServiceFactory implements KeyVaultServiceFactory {
    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider, KeyVaultClientFactory keyVaultClientFactory) {
        KeyVaultService mock = mock(KeyVaultService.class);
//        when(mock.getSecret("pub")).thenReturn("publicSecret");
//        when(mock.getSecret("priv")).thenReturn("privSecret");

        return mock;
    }

    @Override
    public KeyVaultType getType() {
        return KeyVaultType.AZURE;
    }
}

