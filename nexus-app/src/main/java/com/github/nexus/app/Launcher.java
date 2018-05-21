package com.github.nexus.app;

import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

public class Launcher {

    public static final URI SERVER_URI = UriBuilder.fromUri("http://0.0.0.0/").port(8080).build();

    public static void main(String... args) throws Exception {

        Nexus nexus = new Nexus();

        RestServer restServer = RestServerFactory.create().createServer(SERVER_URI, nexus);
        
        restServer.start();
        
        System.in.read();
        
        restServer.stop();
        
        
        System.exit(0); 
    }

}
