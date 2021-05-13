package com.quorum.tessera.config.keypairs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class DirectKeyPairTest {

  private DirectKeyPair keyPair;

  @Before
  public void setUp() {
    keyPair = new DirectKeyPair("public", "private");
  }

  @Test
  public void settingPasswordDoesntDoAnything() {
    keyPair.withPassword("randomPassword".toCharArray());

    assertThat(String.valueOf(keyPair.getPassword())).isEqualTo("");
  }

  @Test
  public void getters() {
    keyPair = new DirectKeyPair("public", "private");

    assertThat(keyPair.getPublicKey()).isEqualTo("public");
    assertThat(keyPair.getPrivateKey()).isEqualTo("private");
  }
}
