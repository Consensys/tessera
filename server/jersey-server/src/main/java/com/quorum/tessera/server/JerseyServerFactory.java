package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * Creates Grizzly and Jersey implementations of the {@link TesseraServer}
 */
public class JerseyServerFactory implements TesseraServerFactory {

    @Override
    public TesseraServer createServer(Config config, Set<Object> services) {
        Application application = services.stream()
                .filter(Application.class::isInstance)
                .findFirst()
                .map(Application.class::cast)
                .get();
        return new JerseyServer(config.getServerConfig(), application);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }

}
