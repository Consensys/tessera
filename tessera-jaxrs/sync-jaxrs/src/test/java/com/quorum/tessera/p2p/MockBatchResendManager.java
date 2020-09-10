package com.quorum.tessera.p2p;

import com.quorum.tessera.p2p.recovery.PushBatchRequest;
import com.quorum.tessera.p2p.recovery.ResendBatchRequest;
import com.quorum.tessera.p2p.recovery.ResendBatchResponse;

public class MockBatchResendManager implements com.quorum.tessera.recovery.workflow.BatchResendManager {
    @Override
    public ResendBatchResponse resendBatch(ResendBatchRequest request) {
        return null;
    }

    @Override
    public void storeResendBatch(PushBatchRequest resendPushBatchRequest) {}
}
