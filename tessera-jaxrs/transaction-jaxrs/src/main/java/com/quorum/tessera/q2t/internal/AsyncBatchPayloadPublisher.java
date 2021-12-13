package com.quorum.tessera.q2t.internal;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.threading.CancellableCountDownLatch;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPublishPayloadException;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import java.util.List;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncBatchPayloadPublisher implements BatchPayloadPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsyncBatchPayloadPublisher.class);

  private final Executor executor;

  private final CancellableCountDownLatchFactory countDownLatchFactory;

  private final PayloadPublisher publisher;

  public AsyncBatchPayloadPublisher(
      ExecutorFactory executorFactory,
      CancellableCountDownLatchFactory countDownLatchFactory,
      PayloadPublisher publisher) {
    this.executor = executorFactory.createCachedThreadPool();
    this.countDownLatchFactory = countDownLatchFactory;
    this.publisher = publisher;
  }

  /**
   * Asynchronously strips (leaving data intended only for that particular recipient) and publishes
   * the payload to each recipient identified by the provided keys.
   *
   * <p>This method blocks until all pushes return successfully; if a push fails with an exception,
   * the method exits immediately and does not wait for the remaining responses.
   *
   * @param payload the payload object to be stripped and pushed
   * @param recipientKeys list of public keys identifying the target nodes
   */
  @Override
  public void publishPayload(EncodedPayload payload, List<PublicKey> recipientKeys) {
    if (recipientKeys.size() == 0) {
      return;
    }

    final CancellableCountDownLatch latch = countDownLatchFactory.create(recipientKeys.size());

    recipientKeys.forEach(
        recipient ->
            executor.execute(
                () -> {
                  try {
                    final EncodedPayload outgoing =
                        EncodedPayload.Builder.forRecipient(payload, recipient).build();
                    publisher.publishPayload(outgoing, recipient);
                    latch.countDown();
                  } catch (RuntimeException e) {
                    LOGGER.info("unable to publish payload in batch: {}", e.getMessage());
                    latch.cancelWithException(e);
                  }
                }));

    try {
      latch.await();
    } catch (InterruptedException e) {
      throw new BatchPublishPayloadException(e);
    }
  }
}
