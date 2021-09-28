package com.quorum.tessera.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

public class StoreRawRequestTest {

  @Test
  public void buildWithSenderAndPayload() {
    PublicKey sender = mock(PublicKey.class);
    byte[] payload = "Payload".getBytes();

    StoreRawRequest result =
        StoreRawRequest.Builder.create()
            .withSender(sender)
            .withPayload(payload)
            .withEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED)
            .build();

    assertThat(result).isNotNull();
    assertThat(result.getSender()).isSameAs(sender);
    assertThat(result.getPayload()).containsExactly(payload);
    assertThat(result.getEncodedPayloadCodec()).isEqualTo(EncodedPayloadCodec.UNSUPPORTED);
  }

  @Test(expected = NullPointerException.class)
  public void buildWithoutPayload() {
    PublicKey sender = mock(PublicKey.class);

    StoreRawRequest.Builder.create()
        .withSender(sender)
        .withEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void buildWithoutSender() {
    byte[] payload = "Payload".getBytes();

    StoreRawRequest.Builder.create()
        .withPayload(payload)
        .withEncodedPayloadCodec(EncodedPayloadCodec.UNSUPPORTED)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void buildEncodedPayloadCodec() {
    PublicKey sender = mock(PublicKey.class);
    byte[] payload = "Payload".getBytes();
    StoreRawRequest.Builder.create().withSender(sender).withPayload(payload).build();
  }
}
