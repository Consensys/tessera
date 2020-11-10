package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import java.util.ServiceLoader;

@Deprecated
// TODO: Remove the p2p clint and related factories.
public interface P2pClientFactory {

    P2pClient create(Config config);

    CommunicationType communicationType();

    static P2pClientFactory newFactory(Config config) {
        return ServiceLoader.load(P2pClientFactory.class).stream()
            .map(ServiceLoader.Provider::get)
                .filter(c -> c.communicationType() == config.getP2PServerConfig().getCommunicationType())
                .findFirst()
                .get();
    }
}
