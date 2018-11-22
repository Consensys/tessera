package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyGeneratorFactoryTest {

    @Test
    public void fileKeyGeneratorWhenKeyVaultConfigNotProvided() {
        final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(null, envProvider);
        when(envProvider.getEnv(anyString())).thenReturn("env");

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator).isExactlyInstanceOf(FileKeyGenerator.class);
    }

    @Test
    public void vaultKeyGeneratorWhenKeyVaultConfigProvided() {
        final KeyVaultConfig keyVaultConfig = new KeyVaultConfig(null, "url");
        final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
        when(envProvider.getEnv(anyString())).thenReturn("env");

        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(keyVaultConfig, envProvider);

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator).isExactlyInstanceOf(AzureVaultKeyGenerator.class);
    }

}
