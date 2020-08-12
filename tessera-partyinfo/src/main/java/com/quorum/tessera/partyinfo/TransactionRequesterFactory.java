package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.Config;

import java.util.ServiceLoader;

public interface TransactionRequesterFactory {

    TransactionRequester createTransactionRequester(Config config);

    static TransactionRequesterFactory newFactory() {
        return ServiceLoader.load(TransactionRequesterFactory.class).findFirst().get();
    }

}
