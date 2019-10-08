package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;


public class MockPartyInfoPollerFactory implements PartyInfoPollerFactory {

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }
    
    
    
}
