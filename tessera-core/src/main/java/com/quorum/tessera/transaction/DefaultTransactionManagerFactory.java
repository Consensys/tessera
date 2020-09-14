package com.quorum.tessera.transaction;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisherFactory;
import com.quorum.tessera.transaction.resend.ResendManager;
import com.quorum.tessera.transaction.resend.ResendManagerImpl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

enum DefaultTransactionManagerFactory implements TransactionManagerFactory {
    INSTANCE;

    private static final AtomicReference<TransactionManager> REF = new AtomicReference<>();

    @Override
    public TransactionManager create(Config config) {

        if (Objects.nonNull(REF.get())) {
            return REF.get();
        }

        PayloadPublisher payloadPublisher = PayloadPublisherFactory.newFactory(config).create(config);
        Enclave enclave = EnclaveFactory.create().create(config);
        EntityManagerDAOFactory entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
        EncryptedTransactionDAO encryptedTransactionDAO = entityManagerDAOFactory.createEncryptedTransactionDAO();
        EncryptedRawTransactionDAO encryptedRawTransactionDAO = entityManagerDAOFactory.createEncryptedRawTransactionDAO();

        ResendManager resendManager = new ResendManagerImpl(encryptedTransactionDAO, enclave);

        TransactionManager transactionManager =
                new TransactionManagerImpl(
                        encryptedTransactionDAO,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        payloadPublisher,
                        100);

        REF.set(transactionManager);
        return transactionManager;
    }

    @Override
    public Optional<TransactionManager> transactionManager() {
        return Optional.ofNullable(REF.get());
    }
}
