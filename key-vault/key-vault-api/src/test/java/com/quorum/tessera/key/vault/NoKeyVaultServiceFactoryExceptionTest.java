package com.quorum.tessera.key.vault;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NoKeyVaultServiceFactoryExceptionTest {

  @Test
  public void createWithMessage() {
    final String msg = "msg";
    VaultSecretNotFoundException exception = new VaultSecretNotFoundException(msg);

    assertThat(exception).hasMessage(msg);
  }
}
