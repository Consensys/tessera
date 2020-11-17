package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.recovery.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.ResendResponse;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;

public class MockLegacyResendManager implements LegacyResendManager {

    @Override
    public ResendResponse resend(final ResendRequest request) {
        return null;
    }
}
