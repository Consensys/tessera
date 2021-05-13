package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeyVaultTypeTest {

  @Test
  public void values() {
    for (KeyVaultType t : KeyVaultType.values()) {
      assertThat(t).isNotNull();
      assertThat(KeyVaultType.valueOf(t.name())).isSameAs(t);
      assertThat(t.getKeyPairType()).isNotNull();
    }
  }
}
