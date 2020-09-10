package com.quorum.tessera.recovery;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.TransactionRequester;
import com.quorum.tessera.partyinfo.TransactionRequesterFactory;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;

public class RecoveryFactoryImpl implements RecoveryFactory {
    @Override
    public Recovery create(Config config) {

        // TODO : Fix this - change to createBatchTransactionRequester
        TransactionRequester transactionRequester =
                TransactionRequesterFactory.newFactory().createTransactionRequester(config);

        StagingEntityDAO stagingEntityDAO = EntityManagerDAOFactory.newFactory(config).createStagingEntityDAO();

        TransactionManager transactionManager = TransactionManagerFactory.create().create(config);

        PayloadEncoder payloadEncoder = PayloadEncoder.create();

        return new RecoveryImpl(
                stagingEntityDAO, Discovery.getInstance(), transactionRequester, transactionManager, payloadEncoder);
    }
}
