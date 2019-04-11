package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * Creates Grizzly and Jersey implementations of the {@link TesseraServer}
 */
public class JerseyServerFactory implements TesseraServerFactory<Object> {

    @Override
    public TesseraServer createServer(ServerConfig serverConfig, Set<Object> services) {
        Application application = services.stream()
                .filter(Application.class::isInstance)
                .filter(serverConfig.getApp().getIntf()::isInstance)
                .findFirst()
                .map(Application.class::cast)
                .get();

        return new JerseyServer(serverConfig, application);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }

}
