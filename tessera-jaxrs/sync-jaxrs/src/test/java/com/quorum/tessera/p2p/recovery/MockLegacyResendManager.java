package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.ResendRequest;
import com.quorum.tessera.transaction.ResendResponse;

public class MockLegacyResendManager implements LegacyResendManager {

    @Override
    public ResendResponse resend(final ResendRequest request) {
        return null;
    }
}
