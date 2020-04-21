package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;

public interface PartyInfoServiceFactory {

    PartyInfoService partyInfoService();

    Enclave enclave();

    PayloadPublisher payloadPublisher();

    ResendBatchPublisher resendBatchPublisher();

    PartyInfoStore partyInfoStore();

    static PartyInfoServiceFactory create() {
        return new PartyInfoServiceFactoryImpl();
    }
}
