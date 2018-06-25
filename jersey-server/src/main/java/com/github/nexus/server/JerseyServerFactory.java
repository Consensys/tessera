package com.github.nexus.server;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import java.net.URI;

public class JerseyServerFactory implements RestServerFactory{

    @Override
    public RestServer createServer(URI uri, Application aplctn, SSLContext sslContext, boolean secure) {
        return new JerseyServer(uri, aplctn, sslContext, secure);
    }
    
}
