package com.github.nexus.app;

import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.LogManager;
import javax.ws.rs.core.UriBuilder;

/**
 * The main entry point for the application.
 * This just starts up the application in the embedded container.
 */
public class Launcher {

    public static final URI SERVER_URI = UriBuilder.fromUri("http://0.0.0.0/").port(8080).build();

    public static void main(String... args) throws Exception {

        try (InputStream loggingConfig = Launcher.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(loggingConfig);
        }
        
        Nexus nexus = new Nexus();

        RestServer restServer = RestServerFactory.create().createServer(SERVER_URI, nexus);
        
        restServer.start();
        
        System.in.read();
        
        restServer.stop();

        System.exit(0); 
    }

}
