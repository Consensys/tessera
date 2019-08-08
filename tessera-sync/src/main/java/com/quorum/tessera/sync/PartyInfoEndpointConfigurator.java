package com.quorum.tessera.sync;

import com.quorum.tessera.core.api.ServiceFactory;
import javax.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoEndpointConfigurator extends ServerEndpointConfig.Configurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoEndpointConfigurator.class);

    private final ServiceFactory serviceFactory = ServiceFactory.create();

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (endpointClass.equals(PartyInfoEndpoint.class)) {
            LOGGER.info("Creating PartyInfoEndpoint {}", endpointClass);
            PartyInfoEndpoint endpoint =
                    new PartyInfoEndpoint(serviceFactory.partyInfoService(), serviceFactory.transactionManager());
            LOGGER.info("Created PartyInfoEndpoint {}", endpoint);

            return (T) endpoint;
        }

        LOGGER.error("Cannot create endpoint for {}", endpointClass);
        throw new InstantiationException(endpointClass + " is not a supported type. ");
    }
}
