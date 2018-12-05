package com.quorum.tessera.key.vault;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultClientFactoryTest {

    @Test
    public void getInstance() {
        KeyVaultClientFactory keyVaultClientFactory = KeyVaultClientFactory.getInstance(KeyVaultType.HASHICORP);

        assertThat(keyVaultClientFactory).isExactlyInstanceOf(MockHashicorpKeyVaultClientFactory.class);
    }

    @Test
    public void instanceNotFoundReturnsNull() {
        assertThat(KeyVaultClientFactory.getInstance(null)).isNull();
    }

}
