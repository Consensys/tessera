package com.quorum.tessera.resteasy;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * Creates RestEasy and Sun HTTP server implementations of the {@link TesseraServer}
 */
public class RestEasyServerFactory implements TesseraServerFactory {

    @Override
    public TesseraServer createServer(Config config, Set<Object> services) {
        Application application = services.stream()
                .filter(Application.class::isInstance)
                .findFirst()
                .map(Application.class::cast)
                .get();
        
        return new RestEasyServer(application, config.getServerConfig());
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.REST;
    }


    
}
