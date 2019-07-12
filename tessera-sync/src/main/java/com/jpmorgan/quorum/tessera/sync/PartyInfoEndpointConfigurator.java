package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.core.api.ServiceFactory;
import javax.websocket.server.ServerEndpointConfig;

public class PartyInfoEndpointConfigurator extends ServerEndpointConfig.Configurator {

    private final ServiceFactory serviceFactory = ServiceFactory.create();

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (PartyInfoEndpoint.class.isAssignableFrom(endpointClass)) {
            return (T) new PartyInfoEndpoint(serviceFactory.partyInfoService(),serviceFactory.encryptedTransactionDAO());
        }
        throw new InstantiationException(endpointClass + " is not a supported type. ");
    }
}
