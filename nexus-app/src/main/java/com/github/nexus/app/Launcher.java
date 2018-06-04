package com.github.nexus.app;

import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import com.github.nexus.service.locator.ServiceLocator;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

/**
 * The main entry point for the application.
 * This just starts up the application in the embedded container.
 */
public class Launcher {

    public static final URI SERVER_URI = UriBuilder.fromUri("http://0.0.0.0/").port(8080).build();

    public static void main(String... args) throws Exception {

        Nexus nexus = new Nexus(ServiceLocator.create());

        RestServer restServer = RestServerFactory.create().createServer(SERVER_URI, nexus);
        
        restServer.start();
        
        System.in.read();
        
        restServer.stop();

        System.exit(0); 
    }

}
