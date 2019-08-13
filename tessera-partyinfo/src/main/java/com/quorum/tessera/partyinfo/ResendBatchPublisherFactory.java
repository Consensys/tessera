
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


public interface ResendBatchPublisherFactory {
    
    Logger LOGGER = LoggerFactory.getLogger(ResendBatchPublisherFactory.class);

    ResendBatchPublisher create(Config config);

    CommunicationType communicationType();

    static ResendBatchPublisherFactory newFactory(Config config) {

        ServerConfig serverConfig = config.getP2PServerConfig();
        List<ResendBatchPublisherFactory> factories = new ArrayList<>();
        Iterator<ResendBatchPublisherFactory> it = ServiceLoader.load(ResendBatchPublisherFactory.class).iterator();
        it.forEachRemaining(factories::add);

        factories.forEach(f -> LOGGER.info("Loaded factory {}", f));

        return factories.stream()
                .filter(f -> f.communicationType() == serverConfig.getCommunicationType())
                .findAny()
                .orElseThrow(
                        () ->
                                new UnsupportedOperationException(
                                        "Unable to create a ResendBatchPublisherFactory for "
                                                + serverConfig.getCommunicationType()));
    }
}
