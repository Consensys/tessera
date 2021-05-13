package com.quorum.tessera.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RuntimeContextExceptionTest {

  @Test
  public void createWithCause() {
    Throwable cause = new Throwable("Ouch that's gonna smart!!");

    RuntimeContextException exception = new RuntimeContextException(cause);

    assertThat(exception.getCause()).isSameAs(cause);
  }
}
