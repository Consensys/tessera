package com.quorum.tessera.recovery;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.loader.ServiceLoaderUtil;

public interface RecoveryFactory {

    Recovery create(Config config);

    static RecoveryFactory newFactory() {
        return ServiceLoaderUtil.load(RecoveryFactory.class)
            .orElse(new RecoveryFactoryImpl());

    }

}
