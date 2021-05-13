package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class AzureKeyVaultConfigTest {

  private AzureKeyVaultConfig keyVaultConfig;

  @Before
  public void setUp() {
    this.keyVaultConfig = new AzureKeyVaultConfig("someurl");
  }

  @Test
  public void getters() {
    assertThat(keyVaultConfig.getUrl()).isEqualTo("someurl");
  }

  @Test
  public void setters() {
    keyVaultConfig.setUrl("newurl");

    assertThat(keyVaultConfig.getUrl()).isEqualTo("newurl");
  }

  @Test
  public void getKeyVaultType() {
    assertThat(keyVaultConfig.getKeyVaultType()).isEqualTo(KeyVaultType.AZURE);
  }
}
