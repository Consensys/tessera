package com.quorum.tessera.key.vault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KeyVaultExceptionTest {

  @Test
  public void createWithMessage() {
    final String msg = "msg";
    KeyVaultException exception = new KeyVaultException(msg);

    assertThat(exception).hasMessage(msg);
  }

  @Test
  public void createWithCause() {
    Throwable cause = new Exception("cause");
    KeyVaultException exception = new KeyVaultException(cause);

    assertThat(exception).hasCause(cause);
  }
}
