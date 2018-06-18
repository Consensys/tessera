
package com.github.nexus.resteasy;

import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import java.net.URI;
import javax.ws.rs.core.Application;


public class RestEasyServerFactory implements RestServerFactory {

    @Override
    public RestServer createServer(URI uri, Application application) {
        return new RestEasyServer(uri, application);
    }
    
}
