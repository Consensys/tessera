package com.quorum.tessera.q2t.internal;

import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.privacygroup.exception.PrivacyGroupPublishException;
import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.threading.CancellableCountDownLatch;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import java.util.List;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncBatchPrivacyGroupPublisher implements BatchPrivacyGroupPublisher {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AsyncBatchPrivacyGroupPublisher.class);

  private final Executor executor;

  private final CancellableCountDownLatchFactory countDownLatchFactory;

  private final PrivacyGroupPublisher publisher;

  public AsyncBatchPrivacyGroupPublisher(
      ExecutorFactory executorFactory,
      CancellableCountDownLatchFactory countDownLatchFactory,
      PrivacyGroupPublisher publisher) {
    this.executor = executorFactory.createCachedThreadPool();
    this.countDownLatchFactory = countDownLatchFactory;
    this.publisher = publisher;
  }

  @Override
  public void publishPrivacyGroup(byte[] data, List<PublicKey> recipientKeys) {

    if (recipientKeys.size() == 0) {
      return;
    }

    final CancellableCountDownLatch latch = countDownLatchFactory.create(recipientKeys.size());

    recipientKeys.forEach(
        key ->
            executor.execute(
                () -> {
                  try {
                    publisher.publishPrivacyGroup(data, key);
                    latch.countDown();
                  } catch (RuntimeException e) {
                    LOGGER.info("Unable to publish privacy group: {}", e.getMessage());
                    latch.cancelWithException(e);
                  }
                }));

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new PrivacyGroupPublishException(e.getMessage());
    }
  }
}
