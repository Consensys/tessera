package com.quorum.tessera.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EncryptorExceptionTest {

  @Test
  public void createInstance() {
    final String message = "HELLOW";
    final EncryptorException exception = new EncryptorException(message);

    assertThat(exception).hasNoCause().hasMessage(message);
  }

  @Test
  public void createInstanceWithNullMessage() {
    final EncryptorException exception = new EncryptorException(null);

    assertThat(exception).hasNoCause();
    assertThat(exception.getMessage()).isNull();
  }
}
