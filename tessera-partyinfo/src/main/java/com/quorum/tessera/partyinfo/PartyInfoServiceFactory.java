package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.enclave.Enclave;

public interface PartyInfoServiceFactory {

    ResendManager resendManager();

    PartyInfoService partyInfoService();

    ConfigService configService();

    Enclave enclave();

    PayloadPublisher payloadPublisher();

    static PartyInfoServiceFactory create() {
        return new PartyInfoServiceFactoryImpl();
    }
}
