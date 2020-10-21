package com.quorum.tessera.recovery.workflow;

import java.util.Optional;

public enum BatchResendManagerHolder {

    INSTANCE;

    private BatchResendManager batchResendManager;

    public BatchResendManager setBatchResendManager(BatchResendManager batchResendManager) {
        this.batchResendManager = batchResendManager;
        return batchResendManager;
    }

    public static BatchResendManagerHolder getInstance() {
        return INSTANCE;
    }

    public Optional<BatchResendManager> getBatchResendManager() {
        return Optional.ofNullable(batchResendManager);
    }

}
