package com.quorum.tessera.transaction.internal;

import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.transaction.PrivacyHelper;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
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
    final EncryptedRawTransactionDAO encryptedRawTransactionDAO =
        EncryptedRawTransactionDAO.create();

    LOGGER.debug("Creating ResendManager");
    final ResendManager resendManager = ResendManager.create();
    LOGGER.debug("Created ResendManager {}", resendManager);

    LOGGER.debug("Creating payload publisher");
    final PayloadPublisher payloadPublisher = PayloadPublisher.create();
    LOGGER.debug("Created payload publisher {}", payloadPublisher);

    LOGGER.debug("Creating batchPayloadPublisher");
    final BatchPayloadPublisher batchPayloadPublisher = BatchPayloadPublisher.create();
    LOGGER.debug("Created batchPayloadPublisher {}", batchPayloadPublisher);

    LOGGER.debug("Creating PrivacyHelper");
    final PrivacyHelper privacyHelper = PrivacyHelper.create();
    LOGGER.debug("Created PrivacyHelper {}", privacyHelper);

    int resendBatchSize = 100;
    LOGGER.debug("Creating PayloadDigest");
    final PayloadDigest messageHashFactory = PayloadDigest.create();
    LOGGER.debug("Created PayloadDigest {}", messageHashFactory);

    return transactionManagerHolder.store(
        new TransactionManagerImpl(
            enclave,
            encryptedTransactionDAO,
            encryptedRawTransactionDAO,
            resendManager,
            batchPayloadPublisher,
            privacyHelper,
            messageHashFactory));
  }
}
