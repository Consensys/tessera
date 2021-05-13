package com.quorum.tessera.key.vault.azure;

import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_ID;
import static com.quorum.tessera.config.util.EnvironmentVariables.AZURE_CLIENT_SECRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.key.vault.KeyVaultService;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;

public class AzureKeyVaultServiceFactoryTest {

  private AzureKeyVaultServiceFactory azureKeyVaultServiceFactory;

  private Config config;

  private EnvironmentVariableProvider envProvider;

  @Before
  public void setUp() {
    this.config = mock(Config.class);
    this.envProvider = mock(EnvironmentVariableProvider.class);
    this.azureKeyVaultServiceFactory = new AzureKeyVaultServiceFactory();
  }

  @Test(expected = NullPointerException.class)
  public void nullConfigThrowsException() {
    azureKeyVaultServiceFactory.create(null, envProvider);
  }

  @Test(expected = NullPointerException.class)
  public void nullEnvVarProviderThrowsException() {
    azureKeyVaultServiceFactory.create(config, null);
  }

  @Test
  public void clientIdEnvironmentVariableNotSetThrowsException() {
    Throwable ex = catchThrowable(() -> azureKeyVaultServiceFactory.create(config, envProvider));

    when(envProvider.getEnv(AZURE_CLIENT_ID)).thenReturn(null);
    when(envProvider.getEnv(AZURE_CLIENT_SECRET)).thenReturn("secret");

    assertThat(ex).isInstanceOf(AzureCredentialNotSetException.class);
    assertThat(ex.getMessage())
        .isEqualTo(
            AZURE_CLIENT_ID + " and " + AZURE_CLIENT_SECRET + " environment variables must be set");
  }

  @Test
  public void clientSecretEnvironmentVariableNotSetThrowsException() {
    Throwable ex = catchThrowable(() -> azureKeyVaultServiceFactory.create(config, envProvider));

    when(envProvider.getEnv(AZURE_CLIENT_ID)).thenReturn("id");
    when(envProvider.getEnv(AZURE_CLIENT_SECRET)).thenReturn(null);

    assertThat(ex).isInstanceOf(AzureCredentialNotSetException.class);
    assertThat(ex.getMessage())
        .isEqualTo(
            AZURE_CLIENT_ID + " and " + AZURE_CLIENT_SECRET + " environment variables must be set");
  }

  @Test
  public void bothClientIdAndClientSecretEnvironmentVariablesNotSetThrowsException() {
    Throwable ex = catchThrowable(() -> azureKeyVaultServiceFactory.create(config, envProvider));

    when(envProvider.getEnv(AZURE_CLIENT_ID)).thenReturn(null);
    when(envProvider.getEnv(AZURE_CLIENT_SECRET)).thenReturn(null);

    assertThat(ex).isInstanceOf(AzureCredentialNotSetException.class);
    assertThat(ex.getMessage())
        .isEqualTo(
            AZURE_CLIENT_ID + " and " + AZURE_CLIENT_SECRET + " environment variables must be set");
  }

  @Test
  public void nullKeyConfigurationThrowsException() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    when(config.getKeys()).thenReturn(null);

    Throwable ex = catchThrowable(() -> azureKeyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage())
        .contains(
            "Trying to create Azure key vault connection but no Azure configuration provided");
  }

  @Test
  public void nullKeyVaultConfigurationThrowsException() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    when(keyConfiguration.getAzureKeyVaultConfig()).thenReturn(null);
    when(config.getKeys()).thenReturn(keyConfiguration);

    Throwable ex = catchThrowable(() -> azureKeyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage())
        .contains(
            "Trying to create Azure key vault connection but no Azure configuration provided");
  }

  @Test
  public void envVarsAndKeyVaultConfigProvidedCreatesAzureKeyVaultService() {
    when(envProvider.getEnv(anyString())).thenReturn("envVar");
    KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(keyVaultConfig.getProperty("url")).thenReturn(Optional.of("URL"));
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE))
        .thenReturn(Optional.of(keyVaultConfig));
    when(config.getKeys()).thenReturn(keyConfiguration);

    KeyVaultService result = azureKeyVaultServiceFactory.create(config, envProvider);

    assertThat(result).isInstanceOf(AzureKeyVaultService.class);
  }

  @Test
  public void getType() {
    assertThat(azureKeyVaultServiceFactory.getType()).isEqualTo(KeyVaultType.AZURE);
  }
}
