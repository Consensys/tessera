package com.jpmorgan.quorum.tessera.sync;

import javax.websocket.server.ServerEndpointConfig;

public class PartyInfoEndpointConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (PartyInfoEndpoint.class.isAssignableFrom(endpointClass)) {
            return (T) new PartyInfoEndpoint(PartyInfoServiceHolder.getPartyInfoService());
        }
        throw new InstantiationException(endpointClass + " is not a supported type. ");
    }
}
