package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * A factory for creating HTTP servers served over a Unix Domain Socket
 */
public class UnixSocketServerFactory implements TesseraServerFactory {

    @Override
    public TesseraServer createServer(final Config config, final Set<Object> services) {
        final Application application = services
            .stream()
            .filter(Application.class::isInstance)
            .findFirst()
            .map(Application.class::cast)
            .get();

        return new UnixSocketServer(config, application);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.UNIX_SOCKET;
    }

}
