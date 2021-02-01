package com.quorum.tessera.privacygroup.publish;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;

public interface PrivacyGroupPublisherFactory {

    PrivacyGroupPublisher create(Config config);

    static PrivacyGroupPublisherFactory newFactory(Config config) {
        return ServiceLoaderUtil.loadAll(PrivacyGroupPublisherFactory.class).findAny().get();
    }
}
