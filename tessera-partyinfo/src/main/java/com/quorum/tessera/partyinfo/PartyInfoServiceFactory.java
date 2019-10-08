package com.quorum.tessera.partyinfo;

public interface PartyInfoServiceFactory {

    PartyInfoService partyInfoService();

    static PartyInfoServiceFactory create() {
        return new PartyInfoServiceFactoryImpl();
    }
}
