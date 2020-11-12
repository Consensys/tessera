package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;

public class ResendBatchPublisherProvider {

    public static ResendBatchPublisher provider() {

        Config config = ConfigFactory.create().getConfig();
        RecoveryClient client = RecoveryClientFactory.newFactory(config).create(config);

        return new RestResendBatchPublisher(client);
    }
}
