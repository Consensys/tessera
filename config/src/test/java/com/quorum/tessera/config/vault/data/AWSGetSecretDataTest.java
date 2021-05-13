package com.quorum.tessera.config.vault.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Test;

public class AWSGetSecretDataTest {
  @Test
  public void getters() {
    AWSGetSecretData data = new AWSGetSecretData("name");

    assertThat(data.getSecretName()).isEqualTo("name");
    assertThat(data.getType()).isEqualTo(KeyVaultType.AWS);
  }
}
