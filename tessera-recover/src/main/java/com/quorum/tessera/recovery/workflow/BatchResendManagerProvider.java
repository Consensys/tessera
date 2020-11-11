package com.quorum.tessera.recovery.workflow;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.recovery.resend.ResendBatchPublisherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchResendManagerProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchResendManagerProvider.class);

    public static BatchResendManager provider() {

        final Config config = ConfigFactory.create().getConfig();

        final Discovery discovery = Discovery.getInstance();
        final Enclave enclave = EnclaveFactory.create().create(config);
        LOGGER.debug("Creating EncryptedTransactionDAO");
        final EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();
        LOGGER.debug("Created EncryptedTransactionDAO {}",encryptedTransactionDAO);

        LOGGER.debug("Creating StagingEntityDAO");
        final StagingEntityDAO stagingEntityDAO = StagingEntityDAO.create();
        LOGGER.debug("Created StagingEntityDAO");

        final ResendBatchPublisher resendBatchPublisher =
            ResendBatchPublisherFactory.newFactory(config).create(config);
        final int defaultMaxResults = 10000;

        return BatchResendManagerHolder.getInstance().setBatchResendManager(new BatchResendManagerImpl(
            enclave,
            stagingEntityDAO,
            encryptedTransactionDAO,
            discovery,
            resendBatchPublisher,
            defaultMaxResults));
    }

}
