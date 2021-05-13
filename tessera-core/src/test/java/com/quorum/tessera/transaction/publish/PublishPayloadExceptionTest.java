package com.quorum.tessera.transaction.publish;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PublishPayloadExceptionTest {

  @Test
  public void createWithMessage() {
    final String msg = "msg";
    PublishPayloadException exception = new PublishPayloadException(msg);

    assertThat(exception).hasMessage(msg);
  }
}
