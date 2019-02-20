package com.quorum.tessera.server.websockets;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.glassfish.tyrus.server.Server;

public class WebSocketServerFactory implements TesseraServerFactory<Class<?>> {

    @Override
    public TesseraServer createServer(ServerConfig config, Set<Class<?>> services) {

        Map<String, Object> properties = new HashMap<>();

        String host = config.getBindingUri().getHost();
        int port = config.getBindingUri().getPort();

        Server server = new Server(host, port, "/", properties, services);

        return new WebSocketServer(server);
    }

    @Override
    public CommunicationType communicationType() {
        return CommunicationType.WEB_SOCKET;
    }

}
