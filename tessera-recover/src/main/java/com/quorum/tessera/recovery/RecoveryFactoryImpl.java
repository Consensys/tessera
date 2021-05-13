package com.quorum.tessera.recovery;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.data.EntityManagerDAOFactory;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.recovery.resend.BatchTransactionRequesterFactory;
import com.quorum.tessera.transaction.TransactionManager;
import com.quorum.tessera.transaction.TransactionManagerFactory;

public class RecoveryFactoryImpl implements RecoveryFactory {
  @Override
  public Recovery create(Config config) {

    BatchTransactionRequester transactionRequester =
        BatchTransactionRequesterFactory.newFactory().createBatchTransactionRequester(config);

    StagingEntityDAO stagingEntityDAO =
        EntityManagerDAOFactory.newFactory(config).createStagingEntityDAO();

    TransactionManager transactionManager = TransactionManagerFactory.create().create(config);

    PayloadEncoder payloadEncoder = PayloadEncoder.create();

    return new RecoveryImpl(
        stagingEntityDAO,
        Discovery.getInstance(),
        transactionRequester,
        transactionManager,
        payloadEncoder);
  }
}
