
package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.apps.TesseraAppClass;


public class PartyInfoEndpointAdapter implements TesseraAppClass {

    @Override
    public Class<?> getAppClass() {
        return PartyInfoEndpoint.class;
    }

    @Override
    public CommunicationType getCommunicationType() {
        return CommunicationType.WEB_SOCKET;
    }

    @Override
    public AppType getAppType() {
        return AppType.P2P;
    }
    
}
