package com.quorum.tessera.transaction.publish;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import java.util.ServiceLoader;

public interface PayloadPublisherFactory {

    PayloadPublisher create(Config config);

    CommunicationType communicationType();

    static PayloadPublisherFactory newFactory(Config config) {
        final CommunicationType commType = config.getP2PServerConfig().getCommunicationType();

        return ServiceLoader.load(PayloadPublisherFactory.class)
            .stream().map(ServiceLoader.Provider::get)
                .filter(f -> f.communicationType() == commType)
                .findAny()
                .orElseThrow(
                        () ->
                                new UnsupportedOperationException(
                                        "Unable to create a PayloadPublisherFactory for " + commType));
    }
}
