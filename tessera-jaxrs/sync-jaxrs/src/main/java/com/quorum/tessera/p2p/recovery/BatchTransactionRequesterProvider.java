package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;

public class BatchTransactionRequesterProvider {

    public static BatchTransactionRequester provider() {
        final Enclave enclave = Enclave.create();

        Config config = ConfigFactory.create().getConfig();
        final RecoveryClient client = RecoveryClientFactory.newFactory(config).create(config);

        return new RestBatchTransactionRequester(enclave,client,100);

    }
}
