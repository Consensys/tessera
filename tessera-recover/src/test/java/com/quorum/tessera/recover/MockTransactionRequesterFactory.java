package com.quorum.tessera.recover;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.sync.TransactionRequester;
import com.quorum.tessera.sync.TransactionRequesterFactory;

import static org.mockito.Mockito.mock;


public class MockTransactionRequesterFactory implements TransactionRequesterFactory {

    private static TransactionRequester transactionRequester;

    static void setTransactionRequester(TransactionRequester tr) {
        transactionRequester = tr;
    }

    @Override
    public TransactionRequester createBatchTransactionRequester(Config config) {
        if(transactionRequester == null) {
            return mock(TransactionRequester.class);
        }
        return transactionRequester;
    }

    @Override
    public TransactionRequester createTransactionRequester(Config config) {
        throw new UnsupportedOperationException("");
    }
}
