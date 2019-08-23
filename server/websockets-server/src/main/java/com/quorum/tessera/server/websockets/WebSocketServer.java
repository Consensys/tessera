package com.quorum.tessera.server.websockets;

import com.jpmorgan.quorum.server.utils.ServerUtils;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.ssl.context.ClientSSLContextFactory;
import java.util.Objects;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.websocket.server.ServerContainer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import static org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer.HTTPCLIENT_ATTRIBUTE;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class WebSocketServer implements TesseraServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

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
        if (serverConfig.isSsl()) {
            SSLContext sslContext =
                    ClientSSLContextFactory.create()
                            .from(serverConfig.getServerUri().toString(), serverConfig.getSslConfig());

            ExtendedJettyClientContainerProvider.setSslContext(sslContext);

            final SslContextFactory ssl = new SslContextFactory.Client();
            ssl.setSslContext(sslContext);

            HttpClient httpClient = new HttpClient(ssl);
            httpClient.start();

            context.getServer().setAttribute(HTTPCLIENT_ATTRIBUTE, httpClient);
        }
        ServerContainer websocketsContainer = WebSocketServerContainerInitializer.configureContext(context);
        LOGGER.info("{}", context.dump());

        websocketsContainer.addEndpoint(StatusEndpoint.class);
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
