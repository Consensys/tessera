package com.quorum.tessera.recover;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;
import com.quorum.tessera.sync.*;
import com.quorum.tessera.transaction.TransactionManager;

public class RecoveryFactoryImpl implements RecoveryFactory {
    @Override
    public Recovery create(Config config) {

        PartyInfoServiceFactory partyInfoServiceFactory = PartyInfoServiceFactory.create(config);

        PartyInfoService partyInfoService = partyInfoServiceFactory.partyInfoService();

        TransactionRequester transactionRequester =
                TransactionRequesterFactory.newFactory().createBatchTransactionRequester(config);

        StagingEntityDAO stagingEntityDAO = EntityManagerDAOFactory.newFactory(config).createStagingEntityDAO();

        TransactionManager transactionManager = TransactionManager.create(config);

        PayloadEncoder payloadEncoder = PayloadEncoder.create();

        return new RecoveryImpl(stagingEntityDAO, partyInfoService, transactionRequester, transactionManager,payloadEncoder);
    }
}
