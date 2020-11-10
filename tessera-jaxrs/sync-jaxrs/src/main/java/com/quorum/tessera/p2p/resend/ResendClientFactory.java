package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import java.util.ServiceLoader;

public interface ResendClientFactory {

    ResendClient create(Config config);

    CommunicationType communicationType();

    static ResendClientFactory newFactory(Config config) {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoader.load(ResendClientFactory.class).stream()
            .map(ServiceLoader.Provider::get)
                .filter(c -> c.communicationType() == config.getP2PServerConfig().getCommunicationType())
                .findFirst()
                .get();
    }
}
