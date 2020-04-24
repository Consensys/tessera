package com.quorum.tessera.p2p;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.transaction.resend.batch.BatchResendManager;

import static org.mockito.Mockito.mock;

public class MockServiceFactory implements ServiceFactory {
    @Override
    public Enclave enclave() {
        return mock(Enclave.class);
    }

    @Override
    public TransactionManager transactionManager() {
        return mock(TransactionManager.class);
    }

    @Override
    public EncryptedTransactionDAO encryptedTransactionDAO() {
        return mock(EncryptedTransactionDAO.class);
    }

    @Override
    public EncryptedRawTransactionDAO encryptedRawTransactionDAO() {
        return mock(EncryptedRawTransactionDAO.class);
    }

    @Override
    public ResendManager resendManager() {
        return mock(ResendManager.class);
    }

    @Override
    public BatchResendManager batchResendManager() {
        return mock(BatchResendManager.class);
    }

    @Override
    public Config config() {
        return mock(Config.class);
    }
}
