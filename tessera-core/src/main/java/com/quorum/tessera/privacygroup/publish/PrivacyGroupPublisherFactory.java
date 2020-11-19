package com.quorum.tessera.privacygroup.publish;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

public interface PrivacyGroupPublisherFactory {

    PrivacyGroupPublisher create(Config config);

    CommunicationType communicationType();

    static PrivacyGroupPublisherFactory newFactory(Config config) {
        final CommunicationType commType = config.getP2PServerConfig().getCommunicationType();

        return ServiceLoaderUtil.loadAll(PrivacyGroupPublisherFactory.class)
            .filter(f -> f.communicationType() == commType)
            .findAny()
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Unable to create a PrivacyGroupPublisherFactory for " + commType));
    }
}
