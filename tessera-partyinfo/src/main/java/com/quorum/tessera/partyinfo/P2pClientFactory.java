package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

@Deprecated
// TODO: Remove the p2p clint and related factories.
public interface P2pClientFactory {

    P2pClient create(Config config);

    CommunicationType communicationType();

    static P2pClientFactory newFactory(Config config) {
        List<P2pClientFactory> all = new ArrayList<>();

        ServiceLoader<P2pClientFactory> serviceLoader = ServiceLoader.load(P2pClientFactory.class);
        serviceLoader.iterator().forEachRemaining(all::add);

        return all.stream()
                .filter(c -> c.communicationType() == config.getP2PServerConfig().getCommunicationType())
                .findFirst()
                .orElse(
                        new P2pClientFactory() {
                            @Override
                            public P2pClient create(Config config) {
                                return null;
                            }

                            @Override
                            public CommunicationType communicationType() {
                                return config.getP2PServerConfig().getCommunicationType();
                            }
                        });
    }
}
