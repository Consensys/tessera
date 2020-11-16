package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;

public class TransactionRequesterFactoryImpl implements TransactionRequesterFactory {

    @Override
    public TransactionRequester createTransactionRequester(Config config) {
        Enclave enclave = Enclave.create();
        ResendClient resendClient = ResendClientFactory.newFactory(config).create(config);
        return new TransactionRequesterImpl(enclave,resendClient);
    }
}
