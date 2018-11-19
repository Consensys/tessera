package com.quorum.tessera.key.generation;

import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.azure.AzureCredentialNotSetException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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
        final KeyVaultConfig keyVaultConfig = new KeyVaultConfig("url");
        final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
        when(envProvider.getEnv(anyString())).thenReturn("env");

        final KeyGenerator keyGenerator = KeyGeneratorFactory.newFactory().create(keyVaultConfig, envProvider);

        assertThat(keyGenerator).isNotNull();
        assertThat(keyGenerator).isExactlyInstanceOf(AzureVaultKeyGenerator.class);
    }

    @Test
    public void exceptionThrownIfKeyVaultConfigButNoClientEnvVars() {
        final KeyVaultConfig keyVaultConfig = new KeyVaultConfig("url");
        final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
        when(envProvider.getEnv("AZURE_CLIENT_ID")).thenReturn(null);
        when(envProvider.getEnv("AZURE_CLIENT_SECRET")).thenReturn(null);

        final KeyGeneratorFactory keyGeneratorFactory = KeyGeneratorFactory.newFactory();

        final Throwable ex = catchThrowable(() -> keyGeneratorFactory.create(keyVaultConfig, envProvider));

        assertThat(ex).isInstanceOf(AzureCredentialNotSetException.class);
        assertThat(ex).hasMessage("AZURE_CLIENT_ID and AZURE_CLIENT_SECRET environment variables must be set");
    }

    @Test
    public void exceptionThrownIfKeyVaultConfigButNoClientIdEnvVar() {
        final KeyVaultConfig keyVaultConfig = new KeyVaultConfig("url");
        final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
        when(envProvider.getEnv("AZURE_CLIENT_ID")).thenReturn(null);
        when(envProvider.getEnv("AZURE_CLIENT_SECRET")).thenReturn("secret");

        final KeyGeneratorFactory keyGeneratorFactory = KeyGeneratorFactory.newFactory();

        final Throwable ex = catchThrowable(() -> keyGeneratorFactory.create(keyVaultConfig, envProvider));

        assertThat(ex).isInstanceOf(AzureCredentialNotSetException.class);
        assertThat(ex).hasMessage("AZURE_CLIENT_ID and AZURE_CLIENT_SECRET environment variables must be set");
    }

    @Test
    public void exceptionThrownIfKeyVaultConfigButNoClientSecretEnvVar() {
        final KeyVaultConfig keyVaultConfig = new KeyVaultConfig("url");
        final EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);
        when(envProvider.getEnv("AZURE_CLIENT_ID")).thenReturn("id");
        when(envProvider.getEnv("AZURE_CLIENT_SECRET")).thenReturn(null);

        final KeyGeneratorFactory keyGeneratorFactory = KeyGeneratorFactory.newFactory();

        final Throwable ex = catchThrowable(() -> keyGeneratorFactory.create(keyVaultConfig, envProvider));

        assertThat(ex).isInstanceOf(AzureCredentialNotSetException.class);
        assertThat(ex).hasMessage("AZURE_CLIENT_ID and AZURE_CLIENT_SECRET environment variables must be set");
    }

}
