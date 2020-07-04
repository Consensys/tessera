package com.quorum.tessera.p2p;


import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;

import static org.mockito.Mockito.mock;

public class MockPartyInfoServiceFactory implements PartyInfoServiceFactory {
    @Override
    public PartyInfoService create(Config config) {
        return mock(PartyInfoService.class);
    }

}
