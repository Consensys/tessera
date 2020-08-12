package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.context.RuntimeContext;

public class RestNodeInfoPublisherFactory implements NodeInfoPublisherFactory {

    @Override
    public NodeInfoPublisher create(final Config config) {
        return new RestNodeInfoPublisher(RuntimeContext.getInstance().getP2pClient(), PartyInfoParser.create());
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
