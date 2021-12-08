package com.quorum.tessera.recovery.workflow.internal;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

public class LegacyResendManagerProvider {

  public static LegacyResendManager provider() {
    final Enclave enclave = Enclave.create();
    final EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();
    final int resendFetchSize = 100;
    final PayloadPublisher payloadPublisher = PayloadPublisher.create();
    final Discovery discovery = Discovery.create();

    return new LegacyResendManagerImpl(
        enclave, encryptedTransactionDAO, resendFetchSize, payloadPublisher, discovery);
  }
}
