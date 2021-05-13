package com.quorum.tessera.privacygroup.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrivacyGroupPublishExceptionTest {

  @Test
  public void createInstance() {
    PrivacyGroupPublishException ex = new PrivacyGroupPublishException("OUCH");
    assertThat(ex).isNotNull();
  }
}
