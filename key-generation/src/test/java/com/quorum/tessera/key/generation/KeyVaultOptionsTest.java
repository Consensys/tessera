package com.quorum.tessera.key.generation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeyVaultOptionsTest {

  @Test
  public void getters() {
    String secretEngineName = "secretEngineName";

    KeyVaultOptions keyVaultOptions = new KeyVaultOptions(secretEngineName);

    assertThat(keyVaultOptions.getSecretEngineName()).isEqualTo(secretEngineName);
  }
}
