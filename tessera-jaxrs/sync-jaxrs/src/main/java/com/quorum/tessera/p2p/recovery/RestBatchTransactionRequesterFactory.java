package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.recovery.resend.BatchTransactionRequesterFactory;

public class RestBatchTransactionRequesterFactory implements BatchTransactionRequesterFactory {

    @Override
    public BatchTransactionRequester createBatchTransactionRequester(Config config) {
        Enclave enclave = EnclaveFactory.create().create(config);
        RecoveryClient recoveryClient = RecoveryClientFactory.newFactory(config).create(config);
        return new RestBatchTransactionRequester(enclave, recoveryClient, 100);
    }
}
