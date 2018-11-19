package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.keypairs.KeyPairType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultServiceFactoryTest {

    @Test
    public void getInstance() {
        KeyVaultServiceFactory keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(KeyPairType.AZURE);

        assertThat(keyVaultServiceFactory).isExactlyInstanceOf(MockAzureKeyVaultServiceFactory.class);
    }

    @Test(expected = NoKeyVaultServiceFactoryException.class)
    public void instanceNotFound() {
        KeyVaultServiceFactory.getInstance(null);
    }

}
