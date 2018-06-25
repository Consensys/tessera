
package com.github.nexus.resteasy;

import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Application;
import java.net.URI;

public class RestEasyServerFactory implements RestServerFactory {

    @Override
    public RestServer createServer(URI uri, Application application, SSLContext sslContext, boolean secure) {
        return new RestEasyServer(uri, application, sslContext, secure);
    }
    
}
