package com.quorum.tessera.transaction;

import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchResponse;
import com.quorum.tessera.partyinfo.PushBatchRequest;


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
}
