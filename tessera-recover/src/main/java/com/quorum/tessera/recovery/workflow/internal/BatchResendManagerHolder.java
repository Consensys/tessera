package com.quorum.tessera.recovery.workflow.internal;

import com.quorum.tessera.recovery.workflow.BatchResendManager;
import java.util.Optional;

enum BatchResendManagerHolder {
  INSTANCE;

  private BatchResendManager batchResendManager;

  public BatchResendManager setBatchResendManager(BatchResendManager batchResendManager) {
    this.batchResendManager = batchResendManager;
    return batchResendManager;
  }

  public Optional<BatchResendManager> getBatchResendManager() {
    return Optional.ofNullable(batchResendManager);
  }
}
