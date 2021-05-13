package com.quorum.tessera.transaction.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EnhancedPrivacyNotSupportedExceptionTest {

  @Test
  public void createInstance() {

    EnhancedPrivacyNotSupportedException ex =
        new EnhancedPrivacyNotSupportedException("not supported");

    assertThat(ex).isNotNull();
  }
}
