package com.quorum.tessera.sync;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

public interface TransactionRequesterFactory {

    default TransactionRequester createBatchTransactionRequester(Config config) {
        Enclave enclave = EnclaveFactory.create().create(config);
        ResendClient resendClient = ResendClientFactory.newFactory(config).create(config);
        return new BatchTransactionRequesterImpl(enclave,resendClient,100);
    }

    default TransactionRequester createTransactionRequester(Config config) {
        Enclave enclave = EnclaveFactory.create().create(config);
        ResendClient resendClient = ResendClientFactory.newFactory(config).create(config);
        return new TransactionRequesterImpl(enclave,resendClient);
    }

    static TransactionRequesterFactory newFactory() {
        return ServiceLoaderUtil.load(TransactionRequesterFactory.class)
            .orElse(new TransactionRequesterFactory() {
        });
    }

}
