package com.quorum.tessera.resteasy;

import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.ssl.context.SSLContextFactory;
import com.quorum.tessera.ssl.context.ServerSSLContextFactory;
import com.sun.net.httpserver.HttpServer;
import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Objects;

/**
 * A RestEasy and Sun HTTP server implementation
 */
public class RestEasyServer implements TesseraServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestEasyServer.class);

    private HttpServer server;

    private final URI uri;

    private final Application application;

    private final SSLContext sslContext;

    private final boolean secure;

    public RestEasyServer(final Application application, final ServerConfig serverConfig) {
        this.uri = serverConfig.getServerUri();
        this.application = application;
       
        this.secure = serverConfig.isSsl();
        if(this.secure) {
            final SSLContextFactory sslContextFactory = ServerSSLContextFactory.create();
            this.sslContext = sslContextFactory.from(
                serverConfig.getServerUri().toString(),
                serverConfig.getSslConfig());
        } else {
            this.sslContext = null;
        }
    }

    @Override
    public void start() throws Exception {
        this.server = HttpServer.create(new InetSocketAddress(this.uri.getPort()), 1);

        final HttpContextBuilder contextBuilder = new HttpContextBuilder();
        contextBuilder.getDeployment().setApplication(this.application);
        contextBuilder.bind(this.server);

        this.server.start();
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Jersey server at {}", uri);

        if (Objects.nonNull(this.server)) {
            this.server.stop(0);
        }

        LOGGER.info("Stopped Jersey server at {}", uri);
    }


}
