package com.quorum.tessera.transaction.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MandatoryRecipientsNotSupportedExceptionTest {

  @Test
  public void createInstance() {

    MandatoryRecipientsNotSupportedException ex =
        new MandatoryRecipientsNotSupportedException("not supported");

    assertThat(ex).isNotNull();
  }
}
