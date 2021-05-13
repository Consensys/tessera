package com.quorum.tessera.config.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class HashicorpVaultKeyPairTest {

  private HashicorpVaultKeyPair keyPair;

  @Before
  public void setUp() {
    keyPair = new HashicorpVaultKeyPair("pubId", "privId", "secretEngine", "secretName", 0);
  }

  @Test
  public void getters() {
    assertThat(keyPair.getPublicKeyId()).isEqualTo("pubId");
    assertThat(keyPair.getPrivateKeyId()).isEqualTo("privId");
    assertThat(keyPair.getSecretEngineName()).isEqualTo("secretEngine");
    assertThat(keyPair.getSecretName()).isEqualTo("secretName");
    assertThat(keyPair.getPublicKey()).isEqualTo(null);
    assertThat(keyPair.getPrivateKey()).isEqualTo(null);
    assertThat(String.valueOf(keyPair.getPassword())).isEqualTo("");
    assertThat(keyPair.getSecretVersion()).isZero();
  }

  @Test
  public void withPasswordDoesNothing() {
    assertThat(String.valueOf(keyPair.getPassword())).isEqualTo("");
    keyPair.withPassword("newpwd".toCharArray());
    assertThat(String.valueOf(keyPair.getPassword())).isEqualTo("");
  }
}
