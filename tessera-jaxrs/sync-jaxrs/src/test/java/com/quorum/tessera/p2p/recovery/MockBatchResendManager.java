package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.recovery.resend.PushBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchRequest;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
import com.quorum.tessera.recovery.workflow.BatchResendManager;

public class MockBatchResendManager implements BatchResendManager {

    @Override
    public ResendBatchResponse resendBatch(ResendBatchRequest request) {
        return null;
    }

    @Override
    public void storeResendBatch(PushBatchRequest resendPushBatchRequest) {}
}
