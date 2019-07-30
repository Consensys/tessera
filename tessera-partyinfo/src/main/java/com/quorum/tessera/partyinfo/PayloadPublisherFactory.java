package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PayloadPublisherFactory {

    Logger LOGGER = LoggerFactory.getLogger(PayloadPublisherFactory.class);

    PayloadPublisher create(Config config);

    CommunicationType communicationType();

    static PayloadPublisherFactory newFactory(Config config) {

        ServerConfig serverConfig = config.getP2PServerConfig();
        List<PayloadPublisherFactory> factories = new ArrayList<>();
        Iterator<PayloadPublisherFactory> it = ServiceLoader.load(PayloadPublisherFactory.class).iterator();
        it.forEachRemaining(factories::add);

        factories.forEach(f -> LOGGER.info("Loaded factory {}", f));

        return factories.stream()
                .filter(f -> f.communicationType() == serverConfig.getCommunicationType())
                .findAny()
                .orElseThrow(
                        () ->
                                new UnsupportedOperationException(
                                        "Unable to create a PayloadPublisherFactory for "
                                                + serverConfig.getCommunicationType()));
    }
}
