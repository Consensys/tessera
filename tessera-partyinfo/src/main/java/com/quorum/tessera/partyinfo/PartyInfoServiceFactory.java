package com.quorum.tessera.partyinfo;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;

public interface PartyInfoServiceFactory {

    PartyInfoService create(Config config);

    static PartyInfoServiceFactory create() {
        return ServiceLoaderUtil.load(PartyInfoServiceFactory.class)
            .orElseGet(() -> new PartyInfoServiceFactoryImpl());

    }
}
