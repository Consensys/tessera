package com.quorum.tessera.q2t;

import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisherFactory;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;

public class AsyncBatchPrivacyGroupPublisherFactory implements BatchPrivacyGroupPublisherFactory {

  @Override
  public BatchPrivacyGroupPublisher create(PrivacyGroupPublisher publisher) {
    ExecutorFactory executorFactory = new ExecutorFactory();
    CancellableCountDownLatchFactory countDownLatchFactory = new CancellableCountDownLatchFactory();

    return new AsyncBatchPrivacyGroupPublisher(executorFactory, countDownLatchFactory, publisher);
  }
}
