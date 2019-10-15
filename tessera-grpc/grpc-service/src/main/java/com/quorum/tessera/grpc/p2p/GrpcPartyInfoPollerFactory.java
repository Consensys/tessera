
package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.partyinfo.PartyInfoPollerFactory;

public class GrpcPartyInfoPollerFactory implements PartyInfoPollerFactory {

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.GRPC;
    }
    
}
