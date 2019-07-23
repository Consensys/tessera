package com.quorum.tessera.partyinfo;

import com.quorum.tessera.admin.ConfigService;
import com.quorum.tessera.enclave.Enclave;

public interface PartyInfoServiceFactory {

    static PartyInfoServiceFactory newFactory() {
        return new PartyInfoServiceFactory() {};
    }

    default PartyInfoService create(Enclave enclave, ConfigService configService) {
        return new PartyInfoServiceImpl(
                configService, enclave, configService.featureToggles().isEnableRemoteKeyValidation());
    }
}
