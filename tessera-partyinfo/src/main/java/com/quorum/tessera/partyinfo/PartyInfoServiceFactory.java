package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.enclave.Enclave;

public interface PartyInfoServiceFactory {

    static PartyInfoServiceFactory newFactory() {
        return new PartyInfoServiceFactory() {};
    }

    default PartyInfoService create(Enclave enclave, ConfigService configService, PayloadPublisher payloadPublisher) {
        return new PartyInfoServiceImpl(configService, enclave, payloadPublisher);
    }
}
