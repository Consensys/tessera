package com.quorum.tessera.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class TesseraExceptionTest {

  @Test
  public void createWithString() {
    TesseraException sample = new MyTesseraException("OUCH");
    assertThat(sample).hasMessage("OUCH");
  }

  static class MyTesseraException extends TesseraException {

    MyTesseraException(String message) {
      super(message);
    }
  }
}
