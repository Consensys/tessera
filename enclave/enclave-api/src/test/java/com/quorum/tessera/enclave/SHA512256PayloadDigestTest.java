package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.Test;

public class SHA512256PayloadDigestTest {
  @Test
  public void digest32Bytes() {
    PayloadDigest digest = new SHA512256PayloadDigest();
    String cipherText = "cipherText";
    byte[] result = digest.digest(cipherText.getBytes());

    // This is what Orion would have generated
    final String expectedB64 = "7AagSZbaNRe/IJzrUKTp8Hl60wncQL1DHvDJCVQ+YIk=";

    assertThat(result).isNotNull();
    assertThat(result).hasSize(32);
    String resultInBase64 = Base64.getEncoder().encodeToString(result);
    assertThat(resultInBase64).isEqualTo(expectedB64);
  }
}
