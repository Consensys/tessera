package com.quorum.tessera.threading;

public class CancellableCountDownLatchFactory {

  public CancellableCountDownLatch create(int count) {
    return new CancellableCountDownLatch(count);
  }
}
