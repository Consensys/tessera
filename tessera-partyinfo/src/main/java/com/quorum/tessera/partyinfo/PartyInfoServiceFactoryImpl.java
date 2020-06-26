package com.quorum.tessera.partyinfo;

import java.util.Objects;

public class PartyInfoServiceFactoryImpl implements PartyInfoServiceFactory {

    private final PartyInfoService partyInfoService;

    public PartyInfoServiceFactoryImpl(PartyInfoService partyInfoService) {
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
    }

    @Override
    public PartyInfoService partyInfoService() {
        return partyInfoService;
    }

}
