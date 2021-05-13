package com.quorum.tessera.threading;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CountDownLatchCancelledExceptionTest {

  @Test
  public void constructor() {
    CountDownLatchCancelledException ex = new CountDownLatchCancelledException();
    assertThat(ex).isNotNull();
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }
}
