package com.quorum.tessera.encryption;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class EncryptorFactoryNotFoundExceptionTest {

  @Test
  public void constructor() {
    final String type = "MYIMPL";
    EncryptorFactoryNotFoundException exception = new EncryptorFactoryNotFoundException(type);

    assertThat(exception).isInstanceOf(RuntimeException.class);
    assertThat(exception)
        .hasMessage("MYIMPL implementation of EncryptorFactory was not found on the classpath");
  }
}
