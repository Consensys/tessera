package com.quorum.tessera.threading;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CancellableCountDownLatchFactoryTest {

  @Test
  public void create() {
    CancellableCountDownLatch countDownLatch = new CancellableCountDownLatchFactory().create(10);
    assertThat(countDownLatch).isNotNull();
    assertThat(countDownLatch.getCount()).isEqualTo(10);
  }
}
