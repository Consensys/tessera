package com.quorum.tessera.transaction.resend.internal;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadDigest;
import com.quorum.tessera.transaction.resend.ResendManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResendManagerProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResendManagerProvider.class);

  public static ResendManager provider() {

    LOGGER.debug("Creating EncryptedTransactionDAO");
    final EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();
    LOGGER.debug("Created EncryptedTransactionDAO {}", encryptedTransactionDAO);

    LOGGER.debug("Creating Enclave");

    final Enclave enclave = Enclave.create();
    LOGGER.debug("Created Enclave {}", enclave);

    PayloadDigest payloadDigest = PayloadDigest.create();

    return new ResendManagerImpl(encryptedTransactionDAO, enclave, payloadDigest);
  }
}
