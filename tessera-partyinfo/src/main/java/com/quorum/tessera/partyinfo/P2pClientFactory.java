package com.quorum.tessera.partyinfo;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

@Deprecated
// TODO: Remove the p2p clint and related factories.
public interface P2pClientFactory {

    P2pClient create(Config config);

    CommunicationType communicationType();

    static P2pClientFactory newFactory(Config config) {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoaderUtil.loadAll(P2pClientFactory.class)
                .filter(c -> c.communicationType() == config.getP2PServerConfig().getCommunicationType())
                .findFirst()
                .get();
    }
}
