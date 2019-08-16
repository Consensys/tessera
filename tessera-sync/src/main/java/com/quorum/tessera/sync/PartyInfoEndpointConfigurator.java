package com.quorum.tessera.sync;

import com.quorum.tessera.core.api.ServiceFactory;
import com.quorum.tessera.partyinfo.PartyInfoServiceFactory;
import javax.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartyInfoEndpointConfigurator extends ServerEndpointConfig.Configurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartyInfoEndpointConfigurator.class);

    private final ServiceFactory serviceFactory = ServiceFactory.create();

    private final PartyInfoServiceFactory partyInfoServiceFactory = PartyInfoServiceFactory.create();

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (endpointClass.equals(PartyInfoEndpoint.class)) {
            LOGGER.debug("Creating PartyInfoEndpoint {}", endpointClass);
            PartyInfoEndpoint endpoint =
                    new PartyInfoEndpoint(
                            partyInfoServiceFactory.partyInfoService(), serviceFactory.transactionManager());
            LOGGER.debug("Created PartyInfoEndpoint {}", endpoint);

            return (T) endpoint;
        }

        if (endpointClass.equals(PartyInfoValidationEndpoint.class)) {
            LOGGER.debug("Creating PartyInfoValidationEndpoint {}", endpointClass);
            PartyInfoValidationEndpoint endpoint =
                    new PartyInfoValidationEndpoint(partyInfoServiceFactory.partyInfoService());
            LOGGER.debug("Created PartyInfoValidationEndpoint {}", endpoint);

            return (T) endpoint;
        }

        LOGGER.error("Cannot create endpoint for {}", endpointClass);
        throw new InstantiationException(endpointClass + " is not a supported type. ");
    }
}
