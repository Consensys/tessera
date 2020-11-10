package com.quorum.tessera.recovery;

import com.quorum.tessera.config.Config;

import java.util.ServiceLoader;

public interface RecoveryFactory {

    Recovery create(Config config);

    static RecoveryFactory newFactory() {
        return ServiceLoader.load(RecoveryFactory.class)
            .findFirst().get();

    }

}
