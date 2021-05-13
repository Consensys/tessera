package com.quorum.tessera.key.vault.hashicorp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class HashicorpVaultExceptionTest {
  @Test
  public void createWithMessage() {
    final String msg = "msg";
    HashicorpVaultException exception = new HashicorpVaultException(msg);

    assertThat(exception).hasMessage(msg);
  }

  @Test
  public void createWithCause() {
    Throwable cause = new Exception("cause");
    HashicorpVaultException exception = new HashicorpVaultException(cause);

    assertThat(exception).hasCause(cause);
  }
}
