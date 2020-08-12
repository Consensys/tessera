package com.quorum.tessera.p2p;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.partyinfo.TransactionRequester;
import com.quorum.tessera.partyinfo.TransactionRequesterFactory;

public class TransactionRequesterFactoryImpl implements TransactionRequesterFactory {

    public TransactionRequester createTransactionRequester(Config config) {
        Enclave enclave = EnclaveFactory.create().create(config);
        ResendClient resendClient = ResendClientFactory.newFactory(config).create(config);
        return new TransactionRequesterImpl(enclave,resendClient);
    }
}
