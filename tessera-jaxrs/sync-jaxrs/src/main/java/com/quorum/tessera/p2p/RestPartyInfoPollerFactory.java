
package com.quorum.tessera.p2p;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.partyinfo.PartyInfoPollerFactory;


public class RestPartyInfoPollerFactory implements PartyInfoPollerFactory {

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
    
}
