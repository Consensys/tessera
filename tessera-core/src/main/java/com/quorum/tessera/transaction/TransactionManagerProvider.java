package com.quorum.tessera.transaction;

import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.resend.ResendManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManagerProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerProvider.class);

    public static TransactionManager provider() {
        final TransactionManagerHolder transactionManagerHolder = TransactionManagerHolder.INSTANCE;
        if (transactionManagerHolder.getTransactionManager().isPresent()) {
            return transactionManagerHolder.getTransactionManager().get();
        }

        final EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();
        final Enclave enclave = Enclave.create();
        final EncryptedRawTransactionDAO encryptedRawTransactionDAO = EncryptedRawTransactionDAO.create();

        LOGGER.debug("Creating ResendManager");
        final ResendManager resendManager = ResendManager.create();
        LOGGER.debug("Created ResendManager {}",resendManager);


        LOGGER.debug("Creating payload publisher");
        final PayloadPublisher payloadPublisher = PayloadPublisher.create();
        LOGGER.debug("Created payload publisher {}",payloadPublisher);

        LOGGER.debug("Creating batchPayloadPublisher");
        final BatchPayloadPublisher batchPayloadPublisher = BatchPayloadPublisherFactory.newFactory().create(payloadPublisher);
        LOGGER.debug("Created batchPayloadPublisher {}",batchPayloadPublisher);

        LOGGER.debug("Creating PrivacyHelper");
        final PrivacyHelper privacyHelper = PrivacyHelper.create();
        LOGGER.debug("Created PrivacyHelper {}",privacyHelper);

        int resendBatchSize = 100;
        LOGGER.debug("Creating MessageHashFactory");
        final MessageHashFactory messageHashFactory = MessageHashFactory.create();
        LOGGER.debug("Created MessageHashFactory {}",messageHashFactory);

        return transactionManagerHolder.store(
            new TransactionManagerImpl(
                encryptedTransactionDAO,
                enclave,
                encryptedRawTransactionDAO,
                resendManager,
                payloadPublisher,
                batchPayloadPublisher,
                privacyHelper,
                resendBatchSize,
                messageHashFactory
            ));

    }

}
