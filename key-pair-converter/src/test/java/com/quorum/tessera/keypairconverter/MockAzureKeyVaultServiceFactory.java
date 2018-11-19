package com.quorum.tessera.keypairconverter;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keypairs.KeyPairType;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import com.quorum.tessera.key.vault.KeyVaultServiceFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockAzureKeyVaultServiceFactory implements KeyVaultServiceFactory {
    @Override
    public KeyVaultService create(Config config, EnvironmentVariableProvider envProvider) {
        KeyVaultService mock = mock(KeyVaultService.class);
        when(mock.getSecret("pub")).thenReturn("publicSecret");
        when(mock.getSecret("priv")).thenReturn("privSecret");

        return mock;
    }

    @Override
    public KeyPairType getType() {
        return KeyPairType.AZURE;
    }
}
