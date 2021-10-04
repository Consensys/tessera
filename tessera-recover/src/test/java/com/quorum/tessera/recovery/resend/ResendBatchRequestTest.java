package com.quorum.tessera.recovery.resend;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class ResendBatchRequestTest {

  @Test
  public void build() {
    ResendBatchRequest request =
        ResendBatchRequest.Builder.create().withPublicKey("publicKey").withBatchSize(100).build();

    assertThat(request).isNotNull();
    assertThat(request.getPublicKey()).isEqualTo("publicKey");
    assertThat(request.getBatchSize()).contains(100);
    assertThat(request.toString()).isNotNull().isNotBlank();
  }

  @Test
  public void validateMissingPublicKey() {
    try {
      ResendBatchRequest.Builder.create().withBatchSize(10).build();
      failBecauseExceptionWasNotThrown(NullPointerException.class);
    } catch (NullPointerException ex) {
      assertThat(ex).hasMessage("publicKey is required");
    }
  }

  @Test
  public void validateMissingBatchSize() {

    ResendBatchRequest result =
        ResendBatchRequest.Builder.create().withPublicKey("SomeKey").build();
    assertThat(result.getPublicKey()).isEqualTo("SomeKey");
    assertThat(result.getBatchSize()).isEmpty();
  }

  @Test
  public void validateZeroBatchSize() {
    try {
      ResendBatchRequest.Builder.create().withPublicKey("SomeKey").withBatchSize(0).build();
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException ex) {
      assertThat(ex).hasMessage("Batch size must be greater than 1");
    }
  }

  @Test
  public void validateNegativeBatchSize() {
    try {
      ResendBatchRequest.Builder.create().withPublicKey("SomeKey").withBatchSize(-100).build();
      failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
    } catch (IllegalArgumentException ex) {
      assertThat(ex).hasMessage("Batch size must be greater than 1");
    }
  }
}
