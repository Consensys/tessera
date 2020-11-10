package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;

import java.util.ServiceLoader;

public interface BatchWorkflowFactory {

    BatchWorkflow create();

    static BatchWorkflowFactory newFactory(
            Enclave enclave,
            PayloadEncoder payloadEncoder,
            Discovery discovery,
            ResendBatchPublisher resendBatchPublisher,
            long transactionCount) {
        return ServiceLoader.load(BatchWorkflowFactory.class).findFirst()
                .orElse(
                        new BatchWorkflowFactoryImpl() {
                            {
                                setEnclave(enclave);
                                setDiscovery(discovery);
                                setPayloadEncoder(payloadEncoder);
                                setResendBatchPublisher(resendBatchPublisher);
                                setTransactionCount(transactionCount);
                            }
                        });
    }
}
