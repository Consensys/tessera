package com.quorum.tessera.p2p;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.transaction.TransactionManager;

import static org.mockito.Mockito.mock;

public class MockServiceFactory implements ServiceFactory {

    @Override
    public TransactionManager transactionManager() {
        return mock(TransactionManager.class);
    }

    @Override
    public Config config() {
        return mock(Config.class);
    }
}
