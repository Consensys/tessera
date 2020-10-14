package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.transaction.ResendRequest;
import com.quorum.tessera.transaction.ResendResponse;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import com.quorum.tessera.transaction.publish.PayloadPublisherFactory;

public interface LegacyResendManager {

    ResendResponse resend(ResendRequest request);

    static LegacyResendManager create(final Config config) {
        return ServiceLoaderUtil.load(LegacyResendManager.class)
            .orElseGet(
                () -> {
                    final Discovery discovery = Discovery.getInstance();
                    final Enclave enclave = EnclaveFactory.create().create(config);
                    final EntityManagerDAOFactory entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
                    final EncryptedTransactionDAO txDao = entityManagerDAOFactory.createEncryptedTransactionDAO();
                    final PayloadEncoder encoder = PayloadEncoder.create();
                    final PayloadPublisher publisher = PayloadPublisherFactory.newFactory(config).create(config);
                    final int maxResults = 1000;

                    return new LegacyResendManagerImpl(enclave, txDao, maxResults, encoder, publisher, discovery);
                }
            );
    }

}
