package com.quorum.tessera.transaction;

import com.quorum.tessera.config.Config;

import java.util.Optional;
import java.util.ServiceLoader;

public interface TransactionManagerFactory {

    TransactionManager create(Config config);

    Optional<TransactionManager> transactionManager();

    static TransactionManagerFactory create() {
        return ServiceLoader.load(TransactionManagerFactory.class)
            .findFirst().get();
    }


}
