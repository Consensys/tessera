package com.quorum.tessera.partyinfo;

public interface PartyInfoServiceFactory {

    ResendManager resendManager();

    PartyInfoService partyInfoService();

    static PartyInfoServiceFactory create() {
        return new PartyInfoServiceFactoryImpl();
    }
}
