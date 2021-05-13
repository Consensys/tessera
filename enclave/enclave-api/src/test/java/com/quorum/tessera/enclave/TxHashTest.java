package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Base64;
import org.junit.Test;

public class TxHashTest {

  @Test
  public void doTests() {

    byte[] data = "SOME_DATA".getBytes();

    String base64Value = Base64.getEncoder().encodeToString(data);

    TxHash hash = new TxHash(base64Value);

    assertThat(hash).isNotNull();

    assertThat(hash.encodeToBase64()).isEqualTo(base64Value);

    assertThat(hash.toString()).isEqualTo(TxHash.class.getSimpleName() + "[" + base64Value + "]");

    assertThat(hash.hashCode()).isEqualTo(Arrays.hashCode(data));

    TxHash secondHash = new TxHash(Base64.getEncoder().encodeToString("OTHERDATA".getBytes()));

    assertThat(hash).isNotEqualTo(secondHash);

    Object bogusHash = new Object();
    assertThat(hash).isNotEqualTo(bogusHash);
  }
}
