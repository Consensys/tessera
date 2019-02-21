package com.quorum.tessera.server.websockets;

import com.quorum.tessera.server.TesseraServer;
import java.util.Objects;
import org.glassfish.tyrus.server.Server;

public class WebSocketServer implements TesseraServer {

    private final Server server;

    public WebSocketServer(Server server) {
        this.server = Objects.requireNonNull(server);
    }
    
    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }

}
