package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DefaultPayloadDigestTest {

  @Test
  public void digest() {
    PayloadDigest digest = new DefaultPayloadDigest();
    String cipherText = "cipherText";
    byte[] result = digest.digest(cipherText.getBytes());

    assertThat(result).isNotNull();
    assertThat(result).hasSize(64);
  }
}
