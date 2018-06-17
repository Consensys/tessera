package com.github.nexus.resteasy;

import com.github.nexus.server.RestServer;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import javax.ws.rs.core.Application;
import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;

public class RestEasyServer implements RestServer {

    private HttpServer server;

    private final URI uri;

    private final Application application;

    public RestEasyServer(URI uri, Application application) {
        this.uri = uri;
        this.application = application;
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
    public void stop() throws Exception {
        server.stop(0);
    }

}
