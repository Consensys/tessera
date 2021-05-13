package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;

public class BatchWorkflowFactoryProvider {

  public static BatchWorkflowFactory provider() {

    Enclave enclave = Enclave.create();
    PayloadEncoder payloadEncoder = PayloadEncoder.create();
    Discovery discovery = Discovery.create();
    ResendBatchPublisher resendBatchPublisher = ResendBatchPublisher.create();

    return new BatchWorkflowFactoryImpl(enclave, payloadEncoder, discovery, resendBatchPublisher);
  }
}
