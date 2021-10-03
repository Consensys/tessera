package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EncodedPayloadCodecTest {

  @Test
  public void current() {
    EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.current();
    assertThat(encodedPayloadCodec).isSameAs(EncodedPayloadCodec.LEGACY);
  }
}
