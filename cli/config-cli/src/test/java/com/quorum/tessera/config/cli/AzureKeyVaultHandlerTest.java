package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.AzureKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import org.junit.Before;
import org.junit.Test;

public class AzureKeyVaultHandlerTest {

  private AzureKeyVaultHandler azureKeyVaultHandler;

  @Before
  public void beforeTest() {
    azureKeyVaultHandler = new AzureKeyVaultHandler();
  }

  @Test
  public void handleWithNullConfigOptions() {
    KeyVaultConfig keyVaultConfig = azureKeyVaultHandler.handle(null);
    assertThat(keyVaultConfig).isNotNull().isExactlyInstanceOf(AzureKeyVaultConfig.class);
    assertThat(keyVaultConfig.getProperties()).isEmpty();
  }

  @Test
  public void handle() {
    String vaultUrl = "vaultUrl";
    KeyVaultConfigOptions keyVaultConfigOptions = mock(KeyVaultConfigOptions.class);
    when(keyVaultConfigOptions.getVaultUrl()).thenReturn(vaultUrl);
    AzureKeyVaultConfig keyVaultConfig =
        (AzureKeyVaultConfig) azureKeyVaultHandler.handle(keyVaultConfigOptions);
    assertThat(keyVaultConfig).isNotNull().isExactlyInstanceOf(AzureKeyVaultConfig.class);
    assertThat(keyVaultConfig.getUrl()).isEqualTo(vaultUrl);
  }
}
