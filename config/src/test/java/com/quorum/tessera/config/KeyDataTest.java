package com.quorum.tessera.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeyDataTest {

  @Test
  public void setPassword() {
    char[] password = "password".toCharArray();
    KeyData keyData = new KeyData();
    keyData.setPassword(password);
    assertThat(password).isEqualTo(password);
  }
}
