
package com.quorum.tessera.client;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;


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
