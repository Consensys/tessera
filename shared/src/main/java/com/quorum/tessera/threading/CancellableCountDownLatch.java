package com.quorum.tessera.threading;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CancellableCountDownLatch extends CountDownLatch {

  private boolean isCancelled;

  private RuntimeException exception;

  public CancellableCountDownLatch(int count) {
    super(count);
  }

  public void cancelWithException(RuntimeException ex) {
    if (getCount() == 0 || isCancelled) {
      return;
    }
    isCancelled = true;
    exception = ex;
    while (getCount() > 0) {
      countDown();
    }
  }

  @Override
  public void await() throws InterruptedException {
    super.await();
    if (isCancelled) {
      throw Optional.ofNullable(exception).orElse(new CountDownLatchCancelledException());
    }
  }

  @Override
  public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
    final boolean result = super.await(timeout, unit);
    if (isCancelled) {
      throw Optional.ofNullable(exception).orElse(new CountDownLatchCancelledException());
    }
    return result;
  }

  boolean isCancelled() {
    return isCancelled;
  }

  RuntimeException getException() {
    return exception;
  }
}
