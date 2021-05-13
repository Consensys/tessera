package com.quorum.tessera.transaction;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisher;
import com.quorum.tessera.transaction.publish.BatchPayloadPublisherFactory;
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
    BatchPayloadPublisher batchPayloadPublisher =
        BatchPayloadPublisherFactory.newFactory().create(payloadPublisher);
    Enclave enclave = EnclaveFactory.create().create(config);
    EntityManagerDAOFactory entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
    EncryptedTransactionDAO encryptedTransactionDAO =
        entityManagerDAOFactory.createEncryptedTransactionDAO();
    EncryptedRawTransactionDAO encryptedRawTransactionDAO =
        entityManagerDAOFactory.createEncryptedRawTransactionDAO();

    PayloadDigest payloadDigest = PayloadDigest.create(config);
    ResendManager resendManager =
        new ResendManagerImpl(encryptedTransactionDAO, enclave, payloadDigest);
    boolean privacyEnabled = config.getFeatures().isEnablePrivacyEnhancements();
    PrivacyHelper privacyHelper = new PrivacyHelperImpl(encryptedTransactionDAO, privacyEnabled);

    TransactionManager transactionManager =
        new TransactionManagerImpl(
            encryptedTransactionDAO,
            enclave,
            encryptedRawTransactionDAO,
            resendManager,
            batchPayloadPublisher,
            privacyHelper,
            payloadDigest);

    REF.set(transactionManager);
    return transactionManager;
  }

  @Override
  public Optional<TransactionManager> transactionManager() {
    return Optional.ofNullable(REF.get());
  }
}
