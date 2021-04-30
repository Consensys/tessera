package com.quorum.tessera.privacygroup;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;

public interface ResidentGroupHandler {

    void onCreate(Config config);

    static ResidentGroupHandler create(Config config) {
        return ServiceLoaderUtil.load(ResidentGroupHandler.class)
                .orElseGet(
                        () -> {
                            final PrivacyGroupManager privacyGroupManager = PrivacyGroupManager.create(config);
                            return new ResidentGroupHandlerImpl(privacyGroupManager);
                        });
    }
}
