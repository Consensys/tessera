package com.quorum.tessera.p2p;

import com.quorum.tessera.partyinfo.PushBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchResponse;

public class MockBatchResendManager implements com.quorum.tessera.recover.resend.BatchResendManager {
    @Override
    public ResendBatchResponse resendBatch(ResendBatchRequest request) {
        return null;
    }

    @Override
    public void storeResendBatch(PushBatchRequest resendPushBatchRequest) {

    }

    @Override
    public boolean isResendMode() {
        return false;
    }

    @Override
    public Result performStaging() {
        return null;
    }

    @Override
    public Result performSync() {
        return null;
    }
}
