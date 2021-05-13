package com.quorum.tessera.config.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class AzureVaultKeyPairTest {

  private AzureVaultKeyPair keyPair;

  @Before
  public void setUp() {
    keyPair = new AzureVaultKeyPair("pubId", "privId", "pubVer", "privVer");
  }

  @Test
  public void getPrivateAndPublicKeysReturnsNull() {
    assertThat(keyPair.getPublicKey()).isNull();
    assertThat(keyPair.getPrivateKey()).isNull();
  }

  @Test
  public void getters() {
    assertThat(keyPair.getPublicKeyId()).isEqualTo("pubId");
    assertThat(keyPair.getPrivateKeyId()).isEqualTo("privId");
    assertThat(keyPair.getPublicKeyVersion()).isEqualTo("pubVer");
    assertThat(keyPair.getPrivateKeyVersion()).isEqualTo("privVer");
  }

  @Test
  public void getPasswordAlwaysReturnsEmptyString() {
    assertThat(keyPair.getPassword()).isEmpty();

    keyPair.withPassword("password".toCharArray());

    assertThat(keyPair.getPassword()).isEmpty();
  }
}
