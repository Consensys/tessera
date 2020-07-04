package com.quorum.tessera.transaction;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;

public interface TransactionManagerFactory {

    TransactionManager create(Config config);

    static TransactionManagerFactory create() {
        return ServiceLoaderUtil.load(TransactionManagerFactory.class)
            .orElse(DefaultTransactionManagerFactory.INSTANCE);
    }


}
