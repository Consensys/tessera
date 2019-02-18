package com.quorum.tessera.server.websockets;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    
    static class Builder {
        
        private ServerConfig config;
        
        private Set<Class<?>> endpoints;
        
        
        public WebSocketServer build() {
            
            Map<String, Object> properties = new HashMap<>();
            
            String host = config.getBindingUri().getHost();
            int port = config.getBindingUri().getPort();
           
            Server server = new Server(host, port, "/", properties, endpoints);
            
            return new WebSocketServer(server);
            
        }
    }
    
    
}
