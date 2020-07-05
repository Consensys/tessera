package com.quorum.tessera.partyinfo;

import com.quorum.tessera.loader.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;

import java.util.Optional;

public interface PartyInfoServiceFactory {

    PartyInfoService create(Config config);

    Optional<PartyInfoService> partyInfoService();

    static PartyInfoServiceFactory create() {
        return ServiceLoaderUtil.load(PartyInfoServiceFactory.class)
            .orElseGet(() -> new PartyInfoServiceFactoryImpl());

    }
}
