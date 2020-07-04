package com.quorum.tessera.transaction;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;

import java.util.Optional;

import static org.mockito.Mockito.mock;

public class MockPartyInfoServiceFactory implements PartyInfoServiceFactory {

    @Override
    public PartyInfoService create(Config config) {
        return mock(PartyInfoService.class);
    }

    @Override
    public Optional<PartyInfoService> partyInfoService() {
        return Optional.of(mock(PartyInfoService.class));
    }
}
