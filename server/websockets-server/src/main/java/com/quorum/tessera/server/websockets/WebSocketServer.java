package com.quorum.tessera.server.websockets;

import com.jpmorgan.quorum.server.utils.ServerUtils;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import java.util.Objects;
import java.util.Set;
import javax.websocket.server.ServerContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

public class WebSocketServer implements TesseraServer {

    private final Server server;

    private final ServerConfig serverConfig;

    private final Set<Class<?>> services;

    public WebSocketServer(ServerConfig serverConfig, Set<Class<?>> services) {
        this.serverConfig = Objects.requireNonNull(serverConfig);
        this.server = ServerUtils.buildWebServer(serverConfig);
        this.services = services;
    }

    @Override
    public void start() throws Exception {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServerContainer websocketsContainer = WebSocketServerContainerInitializer.configureContext(context);

        for (Class<?> service : services) {
            websocketsContainer.addEndpoint(service);
        }

        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }
}
