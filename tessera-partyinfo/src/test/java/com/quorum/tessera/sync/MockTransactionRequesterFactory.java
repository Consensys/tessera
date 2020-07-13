package com.quorum.tessera.sync;

import com.quorum.tessera.config.Config;

import static org.mockito.Mockito.mock;

public class MockTransactionRequesterFactory implements TransactionRequesterFactory {
    @Override
    public TransactionRequester createTransactionRequester(Config config) {
        return mock(TransactionRequester.class);
    }
}
