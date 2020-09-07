package com.quorum.tessera.transaction;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;

import java.util.Optional;

public interface TransactionManagerFactory {

    TransactionManager create(Config config);

    Optional<TransactionManager> transactionManager();

    static TransactionManagerFactory create() {
        return ServiceLoaderUtil.load(TransactionManagerFactory.class)
                .orElse(DefaultTransactionManagerFactory.INSTANCE);
    }
}
