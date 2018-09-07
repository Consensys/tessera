package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import javax.ws.rs.core.Application;
import java.util.stream.Stream;

/**
 * Creates Grizzly and Jersey implementations of the {@link RestServer}
 */
public class JerseyServerFactory implements TesseraServerFactory {

    @Override
    public TesseraServer createServer(ServerConfig serverConfig, Object... args) {
        Application application = Stream.of(args).findFirst()
                .map(Application.class::cast)
                .get();
        return new JerseyServer(serverConfig,application);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }

}
