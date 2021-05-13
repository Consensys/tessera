package com.quorum.tessera.key.vault.aws;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class IncompleteAWSCredentialsExceptionTest {

  @Test
  public void createWithMessage() {
    final String msg = "msg";
    IncompleteAWSCredentialsException exception = new IncompleteAWSCredentialsException(msg);

    assertThat(exception).hasMessage(msg);
  }
}
