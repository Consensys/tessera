package com.quorum.tessera.config.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class UnsupportedKeyPairTest {

  private UnsupportedKeyPair keyPair;

  @Before
  public void setUp() {
    this.keyPair =
        new UnsupportedKeyPair(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null);
  }

  @Test
  public void getPasswordAlwaysReturnsNull() {
    assertThat(keyPair.getPassword()).isNull();

    keyPair.withPassword("password".toCharArray());

    assertThat(keyPair.getPassword()).isNull();
  }

  @Test
  public void versionSetters() {
    assertThat(keyPair.getHashicorpVaultSecretVersion()).isNull();
    assertThat(keyPair.getAzureVaultPublicKeyVersion()).isNull();
    assertThat(keyPair.getAzureVaultPrivateKeyVersion()).isNull();

    keyPair.setHashicorpVaultSecretVersion("1");
    keyPair.setAzureVaultPublicKeyVersion("pubVer");
    keyPair.setAzureVaultPrivateKeyVersion("privVer");

    assertThat(keyPair.getHashicorpVaultSecretVersion()).isEqualTo("1");
    assertThat(keyPair.getAzureVaultPublicKeyVersion()).isEqualTo("pubVer");
    assertThat(keyPair.getAzureVaultPrivateKeyVersion()).isEqualTo("privVer");
  }
}
