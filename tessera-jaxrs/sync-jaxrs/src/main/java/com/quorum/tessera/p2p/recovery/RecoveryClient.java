package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.p2p.resend.ResendClient;

import java.util.ServiceLoader;

public interface RecoveryClient extends ResendClient {

    boolean pushBatch(String targetUrl, PushBatchRequest request);

    ResendBatchResponse makeBatchResendRequest(String targetUrl, ResendBatchRequest request);

    CommunicationType communicationType();

    static RecoveryClient create() {
        Config config = ConfigFactory.create().getConfig();
        CommunicationType communicationType = config.getP2PServerConfig().getCommunicationType();

        return ServiceLoader.load(RecoveryClient.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(c -> c.communicationType() == communicationType)
            .findFirst().get();

    }
}
