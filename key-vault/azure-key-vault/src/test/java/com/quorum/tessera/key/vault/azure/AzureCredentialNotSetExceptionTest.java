package com.quorum.tessera.key.vault.azure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AzureCredentialNotSetExceptionTest {

  @Test
  public void createWithMessage() {
    final String msg = "msg";
    AzureCredentialNotSetException exception = new AzureCredentialNotSetException(msg);

    assertThat(exception).hasMessage(msg);
  }
}
