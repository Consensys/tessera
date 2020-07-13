package com.quorum.tessera.sync;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.loader.ServiceLoaderUtil;

public interface TransactionRequesterFactory {

    TransactionRequester createTransactionRequester(Config config);

    static TransactionRequesterFactory newFactory() {
        return ServiceLoaderUtil.load(TransactionRequesterFactory.class).get();
    }

}
