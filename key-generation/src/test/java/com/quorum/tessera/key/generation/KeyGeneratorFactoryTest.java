package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.KeyVaultConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyGeneratorFactoryTest {

    @Test
    public void vaultKeyGeneratorWhenKeyVaultConfigProvided() {
        final KeyVaultConfig keyVaultConfig = new KeyVaultConfig("url");
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(keyVaultConfig);

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator.getClass()).isEqualTo(VaultKeyGenerator.class);
    }

    @Test
    public void fileKeyGeneratorWhenKeyVaultConfigNotProvided() {
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(null);

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator.getClass()).isEqualTo(FileKeyGenerator.class);
    }
}
