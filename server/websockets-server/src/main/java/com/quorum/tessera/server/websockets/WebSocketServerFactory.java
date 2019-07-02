package com.quorum.tessera.server.websockets;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import java.util.Set;

public class WebSocketServerFactory implements TesseraServerFactory<Class<?>> {

    @Override
    public TesseraServer createServer(ServerConfig config, Set<Class<?>> services) {
        return new WebSocketServer(config, services);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }
}
