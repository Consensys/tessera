package com.quorum.tessera.key.vault.hashicorp;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HashicorpKeyVaultServiceFactoryTest {

    private HashicorpKeyVaultServiceFactory factory;

    private Config config;

    private EnvironmentVariableProvider envProvider;

    @Before
    public void setUp() {
        config = mock(Config.class);
        envProvider = mock(EnvironmentVariableProvider.class);
        factory = new HashicorpKeyVaultServiceFactory();
    }

    @Test(expected = NullPointerException.class)
    public void nullConfigThrowsException() {
        factory.create(null, envProvider);
    }

    @Test(expected = NullPointerException.class)
    public void nullEnvVarProviderThrowsException() {
        factory.create(config, null);
    }

    @Test
    public void createThrowsExceptionIfTokenEnvVarNotSet() {
        Config config = mock(Config.class);
        EnvironmentVariableProvider envProvider = mock(EnvironmentVariableProvider.class);

        when(envProvider.getEnv(anyString())).thenReturn(null);

        Throwable ex = catchThrowable(() -> factory.create(config, envProvider));

        assertThat(ex).isInstanceOf(HashicorpCredentialNotSetException.class);
        assertThat(ex.getMessage()).isEqualTo("HASHICORP_TOKEN must be set");
    }

    @Test
    public void nullKeyConfigurationThrowsException() {
        when(envProvider.getEnv(anyString())).thenReturn("envVar");
        when(config.getKeys()).thenReturn(null);

        Throwable ex = catchThrowable(() -> factory.create(config, envProvider));

        assertThat(ex).isInstanceOf(ConfigException.class);
        assertThat(ex.getMessage()).contains("Trying to create Hashicorp Vault connection but no Vault configuration provided");
    }

    @Test
    public void nullHashicorpVaultConfigThrowsException() {
        when(envProvider.getEnv(anyString())).thenReturn("envVar");
        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(null);

        Throwable ex = catchThrowable(() -> factory.create(config, envProvider));

        assertThat(ex).isInstanceOf(ConfigException.class);
        assertThat(ex.getMessage()).contains("Trying to create Hashicorp Vault connection but no Vault configuration provided");
    }

    @Test
    public void incorrectSyntaxUrlInConfigThrowsException() {
        when(envProvider.getEnv(anyString())).thenReturn("envVar");

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);

        HashicorpKeyVaultConfig vaultConfig = new HashicorpKeyVaultConfig();
        String url = "!@£$%^";
        vaultConfig.setUrl(url);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(vaultConfig);

        Throwable ex = catchThrowable(() -> factory.create(config, envProvider));

        assertThat(ex).isInstanceOf(ConfigException.class);
        assertThat(ex.getMessage()).contains("Provided Hashicorp Vault url is incorrectly formatted");
    }

    @Test
    public void incorrectlyFormattedUrlInConfigThrowsException() {
        when(envProvider.getEnv(anyString())).thenReturn("envVar");

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);

        HashicorpKeyVaultConfig vaultConfig = new HashicorpKeyVaultConfig();
        String url = "notaurl";
        vaultConfig.setUrl(url);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(vaultConfig);

        Throwable ex = catchThrowable(() -> factory.create(config, envProvider));

        assertThat(ex).isInstanceOf(ConfigException.class);
        assertThat(ex.getMessage()).contains("Provided Hashicorp Vault url is incorrectly formatted");
    }

    @Test
    public void createReturnsNewHashicorpKeyVaultService() {
        when(envProvider.getEnv(anyString())).thenReturn("envVar");

        KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
        when(config.getKeys()).thenReturn(keyConfiguration);

        HashicorpKeyVaultConfig vaultConfig = new HashicorpKeyVaultConfig();
        String url = "http://someurl";
        vaultConfig.setUrl(url);
        when(keyConfiguration.getHashicorpKeyVaultConfig()).thenReturn(vaultConfig);

        KeyVaultService result = factory.create(config, envProvider);

        assertThat(result).isInstanceOf(HashicorpKeyVaultService.class);
    }

    @Test
    public void getType() {
        assertThat(factory.getType()).isEqualTo(KeyVaultType.HASHICORP);
    }

}
