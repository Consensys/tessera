package com.quorum.tessera.q2t;

import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

public class AsyncBatchPayloadPublisherFactory implements BatchPayloadPublisherFactory {

  @Override
  public BatchPayloadPublisher create(PayloadPublisher publisher) {
    ExecutorFactory executorFactory = new ExecutorFactory();
    CancellableCountDownLatchFactory countDownLatchFactory = new CancellableCountDownLatchFactory();
    PayloadEncoder encoder = PayloadEncoder.create();
    return new AsyncBatchPayloadPublisher(
        executorFactory, countDownLatchFactory, publisher, encoder);
  }
}
