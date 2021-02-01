package com.quorum.tessera.privacygroup.publish;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.Config;

public interface BatchPrivacyGroupPublisherFactory {

    BatchPrivacyGroupPublisher create(PrivacyGroupPublisher privacyGroupPublisher);

    static BatchPrivacyGroupPublisherFactory newFactory(Config config) {
        return ServiceLoaderUtil.loadAll(BatchPrivacyGroupPublisherFactory.class).findAny().get();
    }
}
