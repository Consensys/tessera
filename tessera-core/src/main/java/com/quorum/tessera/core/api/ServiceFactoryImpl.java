package com.quorum.tessera.core.api;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.transaction.resend.batch.BatchResendManager;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.transaction.TransactionManager;

public class ServiceFactoryImpl implements ServiceFactory {

    private final ServiceLocator serviceLocator = ServiceLocator.create();


    public ServiceFactoryImpl() {}


    @Override
    public Enclave enclave() {
        return find(Enclave.class);
    }

    public <T> T find(Class<T> type) {
        return serviceLocator.getServices().stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Unable to find service type :" + type));
    }

    @Override
    public TransactionManager transactionManager() {
        return find(TransactionManager.class);
    }

    @Override
    public EncryptedTransactionDAO encryptedTransactionDAO() {
        return find(EncryptedTransactionDAO.class);
    }

    @Override
    public EncryptedRawTransactionDAO encryptedRawTransactionDAO() {
        return find(EncryptedRawTransactionDAO.class);
    }

    @Override
    public ResendManager resendManager() {
        return find(ResendManager.class);
    }

    @Override
    public BatchResendManager batchResendManager() {
        return find(BatchResendManager.class);
    }

    @Override
    public Config config() {
        return find(Config.class);
    }


}
