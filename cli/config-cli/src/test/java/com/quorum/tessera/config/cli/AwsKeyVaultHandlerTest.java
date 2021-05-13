package com.quorum.tessera.config.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.DefaultKeyVaultConfig;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.KeyVaultType;
import org.junit.Before;
import org.junit.Test;

public class AwsKeyVaultHandlerTest {

  private AwsKeyVaultHandler keyVaultHandler;

  @Before
  public void beforeTest() {
    keyVaultHandler = new AwsKeyVaultHandler();
  }

  @Test
  public void handleNullConfig() {
    KeyVaultConfig result = keyVaultHandler.handle(null);
    assertThat(result).isNotNull().isExactlyInstanceOf(DefaultKeyVaultConfig.class);
    assertThat(result.getKeyVaultType()).isEqualTo(KeyVaultType.AWS);
    assertThat(result.getProperty("endpoint")).isNotPresent();
  }

  @Test
  public void handleWithVaultUrl() {
    KeyVaultConfigOptions keyVaultConfig = mock(KeyVaultConfigOptions.class);
    String endpointUrl = "http://someurl.com";
    when(keyVaultConfig.getVaultUrl()).thenReturn(endpointUrl);

    KeyVaultConfig result = keyVaultHandler.handle(keyVaultConfig);
    assertThat(result).isNotNull().isExactlyInstanceOf(DefaultKeyVaultConfig.class);
    assertThat(result.getKeyVaultType()).isEqualTo(KeyVaultType.AWS);
    assertThat(result.getProperty("endpoint")).contains(endpointUrl);
  }
}
