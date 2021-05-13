package com.quorum.tessera.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeyExceptionTest {

  @Test
  public void createInstance() {
    final Exception cause = new Exception("OUCH");
    final String message = "HELLOW";
    final KeyException exception = new KeyException(message, cause);

    assertThat(exception).hasCause(cause).hasMessage(message);
  }

  @Test
  public void createInstanceWithNullMessage() {
    final Exception cause = new Exception("OUCH");
    final KeyException exception = new KeyException(null, cause);

    assertThat(exception).hasCause(cause);
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  public void createInstanceWithNullMessageAndNullCause() {
    final KeyException exception = new KeyException(null, null);

    assertThat(exception).hasNoCause();
    assertThat(exception.getMessage()).isNull();
  }

  @Test
  public void createInstanceNullCause() {
    final String message = "HELLOW";
    final KeyException exception = new KeyException(message, null);

    assertThat(exception).hasNoCause().hasMessage(message);
  }
}
