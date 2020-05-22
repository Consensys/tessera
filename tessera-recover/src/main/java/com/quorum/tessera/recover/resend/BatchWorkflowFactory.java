package com.quorum.tessera.recover.resend;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;

public interface BatchWorkflowFactory {


    BatchWorkflow create();

    static BatchWorkflowFactory newFactory(Enclave enclave, PayloadEncoder payloadEncoder, PartyInfoService partyInfoService, ResendBatchPublisher resendBatchPublisher,long transactionCount) {
        return ServiceLoaderUtil.load(BatchWorkflowFactory.class)
            .orElse(new BatchWorkflowFactoryImpl() {{
                setEnclave(enclave);
                setPartyInfoService(partyInfoService);
                setPayloadEncoder(payloadEncoder);
                setResendBatchPublisher(resendBatchPublisher);
                setTransactionCount(transactionCount);
            }});

    }

}
