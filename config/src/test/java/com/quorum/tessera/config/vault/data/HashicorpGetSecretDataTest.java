package com.quorum.tessera.config.vault.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.config.KeyVaultType;
import org.junit.Before;
import org.junit.Test;

public class HashicorpGetSecretDataTest {

  private HashicorpGetSecretData getSecretData;

  @Before
  public void setUp() {
    this.getSecretData = new HashicorpGetSecretData("secret", "secretName", "keyId", 1);
  }

  @Test
  public void getters() {
    assertThat(getSecretData.getSecretEngineName()).isEqualTo("secret");
    assertThat(getSecretData.getSecretName()).isEqualTo("secretName");
    assertThat(getSecretData.getValueId()).isEqualTo("keyId");
    assertThat(getSecretData.getSecretVersion()).isEqualTo(1);
  }

  @Test
  public void getType() {
    assertThat(getSecretData.getType()).isEqualTo(KeyVaultType.HASHICORP);
  }
}
