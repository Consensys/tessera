package com.quorum.tessera.key.vault.azure;

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

  private AzureKeyVaultServiceFactory keyVaultServiceFactory;

  private Config config;

  private EnvironmentVariableProvider envProvider;

  @Before
  public void setUp() {
    this.config = mock(Config.class);
    this.envProvider = null;
    this.keyVaultServiceFactory = new AzureKeyVaultServiceFactory();
  }

  @Test(expected = NullPointerException.class)
  public void nullConfigThrowsException() {
    keyVaultServiceFactory.create(null, envProvider);
  }

  @Test
  public void nullKeyConfigurationThrowsException() {
    when(config.getKeys()).thenReturn(null);

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage())
        .contains(
            "Trying to create Azure key vault connection but no Azure configuration provided");
  }

  @Test
  public void nullKeyVaultConfigurationThrowsException() {
    final KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);

    when(config.getKeys()).thenReturn(keyConfiguration);
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE)).thenReturn(Optional.empty());

    Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage())
        .contains(
            "Trying to create Azure key vault connection but no Azure configuration provided");
  }

  @Test
  public void keyVaultConfigDoesNotContainUrlThrowsException() {
    final KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    final DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);
    when(config.getKeys()).thenReturn(keyConfiguration);
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE))
        .thenReturn(Optional.of(keyVaultConfig));
    when(keyVaultConfig.getProperty(anyString())).thenReturn(Optional.empty());

    final Throwable ex = catchThrowable(() -> keyVaultServiceFactory.create(config, envProvider));

    assertThat(ex).isExactlyInstanceOf(ConfigException.class);
    assertThat(ex.getMessage()).contains("No Azure Key Vault url provided");
  }

  @Test
  public void create() {
    final KeyConfiguration keyConfiguration = mock(KeyConfiguration.class);
    final DefaultKeyVaultConfig keyVaultConfig = mock(DefaultKeyVaultConfig.class);

    when(config.getKeys()).thenReturn(keyConfiguration);
    when(keyConfiguration.getKeyVaultConfig(KeyVaultType.AZURE))
        .thenReturn(Optional.of(keyVaultConfig));
    when(keyVaultConfig.getProperty("url")).thenReturn(Optional.of("http://vaulturl"));

    KeyVaultService result = keyVaultServiceFactory.create(config, envProvider);

    assertThat(result).isInstanceOf(AzureKeyVaultService.class);
  }

  @Test
  public void getType() {
    assertThat(keyVaultServiceFactory.getType()).isEqualTo(KeyVaultType.AZURE);
  }
}
