package com.quorum.tessera.recover.resend;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EncryptedRawTransactionDAO;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.partyinfo.*;
import com.quorum.tessera.transaction.TransactionManager;

import java.util.Optional;

public interface BatchResendManager {

    enum Result {
        SUCCESS,
        PARTIAL_SUCCESS,
        FAILURE
    }

    ResendBatchResponse resendBatch(ResendBatchRequest request);

    void storeResendBatch(PushBatchRequest resendPushBatchRequest);

    boolean isResendMode();

    void cleanupStagingArea();

    Result performStaging();

    Result performSync();


    static BatchResendManager create(Config config) {
        Optional<BatchResendManager> batchResendManagerOptional = ServiceLoaderUtil.load(BatchResendManager.class);

        if(batchResendManagerOptional.isPresent()) {
            return batchResendManagerOptional.get();
        }

        PartyInfoService partyInfoService = PartyInfoServiceFactory.create(config).partyInfoService();
        Enclave enclave = EnclaveFactory.create().create(config);
        TransactionManager transactionManager = TransactionManager.create(config);
        EntityManagerDAOFactory entityManagerDAOFactory = EntityManagerDAOFactory.newFactory(config);
        EncryptedRawTransactionDAO encryptedRawTransactionDAO = entityManagerDAOFactory.createEncryptedRawTransactionDAO();


        EncryptedTransactionDAO encryptedTransactionDAO = entityManagerDAOFactory.createEncryptedTransactionDAO();
        StagingEntityDAO stagingEntityDAO = entityManagerDAOFactory.createStagingEntityDAO();

        return new BatchResendManagerImpl(enclave,transactionManager,stagingEntityDAO,encryptedTransactionDAO,partyInfoService);

    }


}
