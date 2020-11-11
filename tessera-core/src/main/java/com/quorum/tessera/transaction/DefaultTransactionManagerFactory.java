package com.quorum.tessera.transaction;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisherFactory;
import com.quorum.tessera.transaction.resend.ResendManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

enum DefaultTransactionManagerFactory implements TransactionManagerFactory {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTransactionManagerFactory.class);

    private static final AtomicReference<TransactionManager> REF = new AtomicReference<>();

    @Override
    public TransactionManager create(Config config) {

        if (Objects.nonNull(REF.get())) {
            LOGGER.debug("Returning stored TransactionManager {}",REF.get());
            return REF.get();
        }

        LOGGER.debug("Creating TransactionManager");

        PayloadPublisher payloadPublisher = PayloadPublisherFactory.newFactory(config).create(config);

        BatchPayloadPublisher batchPayloadPublisher =
                BatchPayloadPublisherFactory.newFactory().create(payloadPublisher);

        Enclave enclave = EnclaveFactory.create().enclave().get();

        EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();

        EncryptedRawTransactionDAO encryptedRawTransactionDAO = EncryptedRawTransactionDAO.create();

        ResendManager resendManager = ResendManager.create();

        PrivacyHelper privacyHelper = PrivacyHelper.create();

        TransactionManager transactionManager =
                new TransactionManagerImpl(
                        encryptedTransactionDAO,
                        enclave,
                        encryptedRawTransactionDAO,
                        resendManager,
                        payloadPublisher,
                        batchPayloadPublisher,
                        privacyHelper,
                        100);

        LOGGER.debug("Created TransactionManager");
        REF.set(transactionManager);
        return transactionManager;
    }

    @Override
    public Optional<TransactionManager> transactionManager() {
        return Optional.ofNullable(REF.get());
    }
}
