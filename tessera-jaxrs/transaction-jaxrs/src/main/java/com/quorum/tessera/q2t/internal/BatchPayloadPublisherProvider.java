package com.quorum.tessera.q2t.internal;

import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

public class BatchPayloadPublisherProvider {

  public static BatchPayloadPublisher provider() {
    ExecutorFactory executorFactory = new ExecutorFactory();
    CancellableCountDownLatchFactory countDownLatchFactory = new CancellableCountDownLatchFactory();
    PayloadPublisher payloadPublisher = PayloadPublisher.create();
    return new AsyncBatchPayloadPublisher(executorFactory, countDownLatchFactory, payloadPublisher);
  }
}
