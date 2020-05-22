package com.quorum.tessera.recover.resend;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.partyinfo.*;

public interface BatchResendManager {

    ResendBatchResponse resendBatch(ResendBatchRequest request);

    void storeResendBatch(PushBatchRequest resendPushBatchRequest);

    static BatchResendManager create(Config config) {
        return ServiceLoaderUtil.load(BatchResendManager.class)
            .orElseGet(() -> {
                PartyInfoService partyInfoService = PartyInfoServiceFactory.create(config).partyInfoService();
                Enclave enclave = EnclaveFactory.create().create(config);
                EntityManagerDAOFactory entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);

                EncryptedTransactionDAO encryptedTransactionDAO = entityManagerDAOFactory.createEncryptedTransactionDAO();
                StagingEntityDAO stagingEntityDAO = entityManagerDAOFactory.createStagingEntityDAO();

                ResendBatchPublisher resendBatchPublisher = ResendBatchPublisherFactory.newFactory(config).create(config);

                return new BatchResendManagerImpl(
                    enclave, stagingEntityDAO, encryptedTransactionDAO, partyInfoService,resendBatchPublisher);
            });
    }
}
