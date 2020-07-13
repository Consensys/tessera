package com.quorum.tessera.p2p;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.sync.ResendClient;
import com.quorum.tessera.sync.ResendClientFactory;
import com.quorum.tessera.sync.TransactionRequester;
import com.quorum.tessera.sync.TransactionRequesterFactory;

public class TransactionRequesterFactoryImpl implements TransactionRequesterFactory {
    @Override
    public TransactionRequester createTransactionRequester(Config config) {
        Enclave enclave = EnclaveFactory.create().create(config);
        ResendClient resendClient = ResendClientFactory.newFactory(config).create(config);
        return new TransactionRequesterImpl(enclave,resendClient);
    }
}
