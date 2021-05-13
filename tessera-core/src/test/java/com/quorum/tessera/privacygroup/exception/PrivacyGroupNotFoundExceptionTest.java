package com.quorum.tessera.privacygroup.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrivacyGroupNotFoundExceptionTest {

  @Test
  public void createInstance() {

    PrivacyGroupNotFoundException ex = new PrivacyGroupNotFoundException("not found");
    assertThat(ex).isNotNull();
  }
}
