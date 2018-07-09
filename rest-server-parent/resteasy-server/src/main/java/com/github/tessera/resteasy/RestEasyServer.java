package com.github.tessera.resteasy;

import com.github.tessera.config.ServerConfig;
import com.github.tessera.server.RestServer;
import com.github.tessera.ssl.context.SSLContextFactory;
import com.github.tessera.ssl.context.ServerSSLContextFactory;
import com.sun.net.httpserver.HttpServer;
import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import java.net.InetSocketAddress;
import java.net.URI;

public class RestEasyServer implements RestServer {

    private HttpServer server;

    private final URI uri;

    private final Application application;

    private final SSLContext sslContext;

    private final boolean secure;

    private final SSLContextFactory sslContextFactory = ServerSSLContextFactory.create();

    public RestEasyServer(URI uri, Application application, ServerConfig serverConfig) {
        this.uri = uri;
        this.application = application;
       
        this.secure = serverConfig.isSsl();
        if(this.secure) {
             this.sslContext = sslContextFactory.from(serverConfig.getSslConfig());
        } else {
            this.sslContext = null;
        }
    }


    @Override
    public void start() throws Exception {

        server = HttpServer.create(new InetSocketAddress(uri.getPort()), 1);
        HttpContextBuilder contextBuilder = new HttpContextBuilder();
        contextBuilder.getDeployment().setApplication(application);

        contextBuilder.bind(server);
        server.start();

    }

    @Override
    public void stop() {
        server.stop(0);
    }

}
