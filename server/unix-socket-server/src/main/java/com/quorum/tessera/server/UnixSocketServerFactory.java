package com.quorum.tessera.server;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;

import javax.ws.rs.core.Application;
import java.util.Set;

/**
 * A factory for creating HTTP servers served over a Unix Domain Socket
 */
public class UnixSocketServerFactory implements TesseraServerFactory {

    @Override
    public TesseraServer createServer(final ServerConfig serverConfig, final Set<Object> services) {

        if (System.getProperty("spring.profiles.active", "").contains("disable-unixsocket")) {
            return null;
        }

        final Application application = services
            .stream()
            .filter(Application.class::isInstance)
            .filter(serverConfig.getApp().getIntf()::isInstance)
            .findFirst()
            .map(Application.class::cast)
            .get();

        return new UnixSocketServer(serverConfig, application);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.UNIX_SOCKET;
    }

}
