package com.github.nexus.server;

import java.net.URI;
import javax.ws.rs.core.Application;

public class JerseyServerFactory implements RestServerFactory{

    @Override
    public RestServer createServer(URI uri, Application aplctn) {
        return new JerseyServer(uri, aplctn);
    }
    
}
