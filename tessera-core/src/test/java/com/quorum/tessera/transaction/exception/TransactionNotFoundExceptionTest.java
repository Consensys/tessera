package com.quorum.tessera.transaction.exception;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TransactionNotFoundExceptionTest {

  @Test
  public void constructWithMessage() {

    final String message = "Some punk's busted up my ride!!";

    final TransactionNotFoundException testException = new TransactionNotFoundException(message);

    Assertions.assertThat(testException.getMessage()).isEqualTo(message);
  }
}
