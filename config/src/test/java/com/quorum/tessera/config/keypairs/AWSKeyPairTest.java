package com.quorum.tessera.config.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class AWSKeyPairTest {

  private AWSKeyPair keyPair;

  @Before
  public void setUp() {
    keyPair = new AWSKeyPair("pubId", "privId");
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
  }

  @Test
  public void getPasswordAlwaysReturnsEmptyString() {
    assertThat(keyPair.getPassword()).isEmpty();

    keyPair.withPassword("password".toCharArray());

    assertThat(keyPair.getPassword()).isEmpty();
  }
}
