package com.quorum.tessera.client;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.P2pClientFactory;

public class GrpcP2pClientFactory implements P2pClientFactory {

    @Override
    public P2pClient create(Config config) {
        return new GrpcP2pClient();
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.GRPC;
    }
}
