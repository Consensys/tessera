package com.quorum.tessera.recovery.workflow.internal;

import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.staging.StagingEntityDAO;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.recovery.workflow.BatchWorkflowFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchResendManagerProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatchResendManagerProvider.class);

  public static BatchResendManager provider() {

    if (BatchResendManagerHolder.INSTANCE.getBatchResendManager().isPresent()) {
      return BatchResendManagerHolder.INSTANCE.getBatchResendManager().get();
    }

    LOGGER.debug("Creating EncryptedTransactionDAO");
    final EncryptedTransactionDAO encryptedTransactionDAO = EncryptedTransactionDAO.create();
    LOGGER.debug("Created EncryptedTransactionDAO {}", encryptedTransactionDAO);

    LOGGER.debug("Creating StagingEntityDAO");
    final StagingEntityDAO stagingEntityDAO = StagingEntityDAO.create();
    LOGGER.debug("Created StagingEntityDAO");

    final int defaultMaxResults = 10000;

    BatchWorkflowFactory batchWorkflowFactory = BatchWorkflowFactory.create();

    BatchResendManager batchResendManager =
        new BatchResendManagerImpl(
            stagingEntityDAO, encryptedTransactionDAO, defaultMaxResults, batchWorkflowFactory);

    return BatchResendManagerHolder.INSTANCE.setBatchResendManager(batchResendManager);
  }
}
