package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.recovery.resend.*;

import java.util.ServiceLoader;

public interface BatchResendManager {

    ResendBatchResponse resendBatch(ResendBatchRequest request);

    void storeResendBatch(PushBatchRequest resendPushBatchRequest);

    static BatchResendManager create(Config config) {
        return ServiceLoader.load(BatchResendManager.class).findFirst()
                .orElseGet(
                        () -> {
                            Discovery discovery = Discovery.getInstance();
                            Enclave enclave = EnclaveFactory.create().create(config);
                            EntityManagerDAOFactory entityManagerDAOFactory =
                                    EntityManagerDAOFactory.newFactory(config);

                            EncryptedTransactionDAO encryptedTransactionDAO =
                                    entityManagerDAOFactory.createEncryptedTransactionDAO();
                            StagingEntityDAO stagingEntityDAO = entityManagerDAOFactory.createStagingEntityDAO();

                            ResendBatchPublisher resendBatchPublisher =
                                    ResendBatchPublisherFactory.newFactory(config).create(config);

                            final int defaultMaxResults = 10000;
                            return BatchResendManagerHolder.getInstance().setBatchResendManager(new BatchResendManagerImpl(
                                    enclave,
                                    stagingEntityDAO,
                                    encryptedTransactionDAO,
                                    discovery,
                                    resendBatchPublisher,
                                    defaultMaxResults));
                        });

    }

    static BatchResendManager getInstance() {
        return BatchResendManagerHolder.getInstance().getBatchResendManager().orElseThrow(() -> new IllegalStateException("BatchResendManager has not been initialised"));
    }


}
