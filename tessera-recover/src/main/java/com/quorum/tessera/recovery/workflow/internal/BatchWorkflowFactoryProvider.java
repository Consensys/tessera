package com.quorum.tessera.recovery.workflow.internal;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.recovery.workflow.BatchWorkflowFactory;

public class BatchWorkflowFactoryProvider {

  public static BatchWorkflowFactory provider() {

    Enclave enclave = Enclave.create();
    Discovery discovery = Discovery.create();
    ResendBatchPublisher resendBatchPublisher = ResendBatchPublisher.create();

    return new BatchWorkflowFactoryImpl(enclave, discovery, resendBatchPublisher);
  }
}
