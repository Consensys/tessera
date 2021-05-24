package com.quorum.tessera.q2t.internal;

import com.quorum.tessera.privacygroup.publish.BatchPrivacyGroupPublisher;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import com.quorum.tessera.threading.CancellableCountDownLatchFactory;
import com.quorum.tessera.threading.ExecutorFactory;

public class BatchPrivacyGroupPublisherProvider {

  public static BatchPrivacyGroupPublisher provider() {
    PrivacyGroupPublisher privacyGroupPublisher = PrivacyGroupPublisher.create();
    ExecutorFactory executorFactory = new ExecutorFactory();
    CancellableCountDownLatchFactory countDownLatchFactory = new CancellableCountDownLatchFactory();

    return new AsyncBatchPrivacyGroupPublisher(
        executorFactory, countDownLatchFactory, privacyGroupPublisher);
  }
}
