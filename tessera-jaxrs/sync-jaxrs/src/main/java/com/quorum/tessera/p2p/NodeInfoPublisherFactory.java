package com.quorum.tessera.p2p;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

public interface NodeInfoPublisherFactory {

    NodeInfoPublisher create(Config config);

    CommunicationType communicationType();

    static NodeInfoPublisherFactory newFactory(Config config) {
        final CommunicationType commType = config.getP2PServerConfig().getCommunicationType();

        return ServiceLoaderUtil.loadAll(NodeInfoPublisherFactory.class)
            .filter(f -> f.communicationType() == commType)
            .findAny()
            .orElseThrow(() -> new UnsupportedOperationException("Unable to create a NodeInfoPublisherFactory for " + commType));
    }
}
