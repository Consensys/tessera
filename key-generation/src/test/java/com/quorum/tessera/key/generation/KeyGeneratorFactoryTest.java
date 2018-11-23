package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.AzureKeyVaultConfig;
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
        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(null);
        when(envProvider.getEnv(anyString())).thenReturn("env");

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator).isExactlyInstanceOf(FileKeyGenerator.class);
    }

    @Test
    public void azureVaultKeyGeneratorWhenKeyVaultConfigProvided() {
        final AzureKeyVaultConfig keyVaultConfig = new AzureKeyVaultConfig("url");

        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(keyVaultConfig);

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator).isExactlyInstanceOf(AzureVaultKeyGenerator.class);
    }

}
