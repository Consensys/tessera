package com.quorum.tessera.config.vault.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Test;

public class AzureGetSecretDataTest {
  @Test
  public void getters() {
    AzureGetSecretData data = new AzureGetSecretData("name", "version");

    assertThat(data.getSecretName()).isEqualTo("name");
    assertThat(data.getSecretVersion()).isEqualTo("version");
    assertThat(data.getType()).isEqualTo(KeyVaultType.AZURE);
  }
}
