package com.quorum.tessera.recovery.resend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Test;

public class ResendBatchRequestTest {

  @Test
  public void build() {
    ResendBatchRequest request =
        ResendBatchRequest.Builder.create().withPublicKey("publicKey").withBatchSize(100).build();

    assertThat(request).isNotNull();
    assertThat(request.getPublicKey()).isEqualTo("publicKey");
    assertThat(request.getBatchSize()).isEqualTo(100);
  }

  @Test
  public void validate() {

    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> ResendBatchRequest.Builder.create().build());
  }
}
