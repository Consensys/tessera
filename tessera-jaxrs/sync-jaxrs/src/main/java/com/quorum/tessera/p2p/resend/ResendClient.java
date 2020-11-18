package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;

import java.util.ServiceLoader;

/**
 * A client that can be used to make resend requests to other nodes. It cannot make requests to other endpoints and may
 * have different timeouts than P2P clients.
 */
public interface ResendClient {

    boolean makeResendRequest(String targetUrl, ResendRequest request);

    CommunicationType communicationType();

    static ResendClient create() {
        Config config = ConfigFactory.create().getConfig();
        CommunicationType communicationType = config.getP2PServerConfig().getCommunicationType();

        return ServiceLoader.load(ResendClient.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(c -> c.communicationType() == communicationType)
            .findFirst().get();

    }
}
