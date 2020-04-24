package com.quorum.tessera.p2p;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.partyinfo.*;

import static org.mockito.Mockito.mock;

public class MockPartyInfoServiceFactory implements PartyInfoServiceFactory {
    @Override
    public PartyInfoService partyInfoService() {
        return mock(PartyInfoService.class);
    }

    @Override
    public Enclave enclave() {
        return mock(Enclave.class);
    }

}
