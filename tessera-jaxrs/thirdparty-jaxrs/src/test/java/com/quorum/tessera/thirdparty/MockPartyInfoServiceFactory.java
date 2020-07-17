package com.quorum.tessera.thirdparty;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.*;

import static org.mockito.Mockito.mock;

public class MockPartyInfoServiceFactory implements PartyInfoServiceFactory {
    @Override
    public PartyInfoService create(Config config) {
        return mock(PartyInfoService.class);
    }
}
