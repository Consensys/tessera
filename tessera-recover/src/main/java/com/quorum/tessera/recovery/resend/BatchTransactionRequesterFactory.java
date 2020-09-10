package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.config.Config;

import java.util.ServiceLoader;

public interface BatchTransactionRequesterFactory {

    BatchTransactionRequester createBatchTransactionRequester(Config config);

    static BatchTransactionRequesterFactory newFactory() {
        return ServiceLoader.load(BatchTransactionRequesterFactory.class).findFirst().get();
    }
}
