package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.apps.TesseraApp;

import java.util.Set;
import javax.ws.rs.core.Application;

/** Creates Grizzly and Jersey implementations of the {@link TesseraServer} */
public class JerseyServerFactory implements TesseraServerFactory<Object> {

    @Override
    public TesseraServer createServer(ServerConfig serverConfig, Set<Object> services) {
        Application application =
                services.stream()
                        .filter(TesseraApp.class::isInstance)
                        .filter(Application.class::isInstance)
                        .map(TesseraApp.class::cast)
                        .filter(a -> a.getAppType().equals(serverConfig.getApp()))
                        .map(Application.class::cast)
                        .findFirst()
                        .get();

        return new JerseyServer(serverConfig, application);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }
}
