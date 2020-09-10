package com.quorum.tessera.recovery;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;

import static org.mockito.Mockito.mock;

public class MockPartyInfoServiceFactory implements PartyInfoServiceFactory {

    private static final ThreadLocal<PartyInfoService> partyInfoServiceThreadLocal = ThreadLocal.withInitial(() -> mock(PartyInfoService.class));

    @Override
    public PartyInfoService partyInfoService() {
        return partyInfoServiceThreadLocal.get();
    }
}
