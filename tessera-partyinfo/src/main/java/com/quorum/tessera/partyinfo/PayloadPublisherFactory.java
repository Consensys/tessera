package com.quorum.tessera.partyinfo;

import com.quorum.tessera.ServiceLoaderUtil;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;


public interface PayloadPublisherFactory {

    PayloadPublisher create(Config config);

    CommunicationType communicationType();

    static PayloadPublisherFactory newFactory(Config config) {

        final CommunicationType commType = config.getP2PServerConfig().getCommunicationType();

        return ServiceLoaderUtil.loadAll(PayloadPublisherFactory.class)
                .filter(f -> f.communicationType() == commType)

                .findAny()
                .orElseThrow(
                        () ->
                                new UnsupportedOperationException(
                                        "Unable to create a PayloadPublisherFactory for " + commType));
    }
}
