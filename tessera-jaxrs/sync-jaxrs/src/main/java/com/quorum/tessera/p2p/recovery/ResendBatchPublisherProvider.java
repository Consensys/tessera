package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.recovery.resend.ResendBatchPublisher;

public class ResendBatchPublisherProvider {

    public static ResendBatchPublisher provider() {

        RecoveryClient client = RecoveryClient.create();

        return new RestResendBatchPublisher(client);
    }
}
