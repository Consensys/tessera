package com.quorum.tessera.transaction.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StoreEntityExceptionTest {

  @Test
  public void testInit() {
    Exception cause = new Exception("Ouch");
    StoreEntityException ex = new StoreEntityException("Message", cause);
    assertThat(ex).hasCause(cause);
    assertThat(ex).hasMessage("Message");
  }
}
