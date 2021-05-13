package com.quorum.tessera.transaction.publish;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class BatchPublishPayloadExceptionTest {

  @Test
  public void constructor() {
    final RuntimeException root = new RuntimeException("root cause");
    BatchPublishPayloadException exception = new BatchPublishPayloadException(root);

    assertThat(exception).hasCause(root);
  }
}
