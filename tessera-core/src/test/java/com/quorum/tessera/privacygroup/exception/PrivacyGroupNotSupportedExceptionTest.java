package com.quorum.tessera.privacygroup.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrivacyGroupNotSupportedExceptionTest {

  @Test
  public void createInstance() {
    PrivacyGroupNotSupportedException ex = new PrivacyGroupNotSupportedException("OUCH");
    assertThat(ex).isNotNull();
  }
}
