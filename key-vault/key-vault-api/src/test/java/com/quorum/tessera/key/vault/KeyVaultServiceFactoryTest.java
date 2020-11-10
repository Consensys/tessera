package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultServiceFactoryTest {

    @Ignore
    @Test
    public void getInstance() {
        KeyVaultServiceFactory keyVaultServiceFactory = KeyVaultServiceFactory.getInstance(KeyVaultType.AZURE);

        assertThat(keyVaultServiceFactory).isExactlyInstanceOf(MockAzureKeyVaultServiceFactory.class);
    }

    @Test(expected = NoKeyVaultServiceFactoryException.class)
    public void instanceNotFound() {
        KeyVaultServiceFactory.getInstance(null);
    }

}
