package com.quorum.tessera.thirdparty;

import com.quorum.tessera.partyinfo.*;

import static org.mockito.Mockito.mock;

public class MockPartyInfoServiceFactory implements PartyInfoServiceFactory {
    @Override
    public PartyInfoService partyInfoService() {
        return mock(PartyInfoService.class);
    }

}
