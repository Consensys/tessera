package com.quorum.tessera.recovery;

import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.recovery.resend.BatchTransactionRequester;
import com.quorum.tessera.transaction.TransactionManager;

public class RecoveryProvider {

    public static Recovery provider() {

        StagingEntityDAO stagingEntityDAO = StagingEntityDAO.create();
        Discovery discovery = Discovery.create();
        BatchTransactionRequester batchTransactionRequester = BatchTransactionRequester.create();

        TransactionManager transactionManager = TransactionManager.create();

        PayloadEncoder payloadEncoder = PayloadEncoder.create();

        return new RecoveryImpl(stagingEntityDAO,discovery,batchTransactionRequester,transactionManager,payloadEncoder);
    }
}
