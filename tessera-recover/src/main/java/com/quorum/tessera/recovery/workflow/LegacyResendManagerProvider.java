package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.transaction.publish.PayloadPublisher;

public class LegacyResendManagerProvider {

    public static LegacyResendManager provider() {
        final Enclave enclave = Enclave.create();
        final EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();
        final int resendFetchSize = 100;
        final PayloadEncoder payloadEncoder = PayloadEncoder.create();
        final PayloadPublisher payloadPublisher = PayloadPublisher.create();
        final Discovery discovery = Discovery.create();

        return new LegacyResendManagerImpl(enclave,encryptedTransactionDAO,resendFetchSize,payloadEncoder,payloadPublisher,discovery);
    }

}
