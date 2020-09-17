package com.quorum.tessera.p2p.resend;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.loader.ServiceLoaderUtil;

public interface ResendClientFactory {

    ResendClient create(Config config);

    CommunicationType communicationType();

    static ResendClientFactory newFactory(Config config) {
        // TODO: return the stream and let the caller deal with it
        return ServiceLoaderUtil.loadAll(ResendClientFactory.class)
                .filter(c -> c.communicationType() == config.getP2PServerConfig().getCommunicationType())
                .findFirst()
                .get();
    }
}
