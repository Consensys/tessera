package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * Creates Grizzly and Jersey implementations of the {@link RestServer}
 */
public class JerseyServerFactory implements TesseraServerFactory {

    @Override
    public TesseraServer createServer(ServerConfig serverConfig, Set<Object> services) {
        Application application = services.stream()
                .filter(Application.class::isInstance)
                .findFirst()
                .map(Application.class::cast)
                .get();
        return new JerseyServer(serverConfig,application);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }

}
