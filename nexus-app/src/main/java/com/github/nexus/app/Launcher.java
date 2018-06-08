package com.github.nexus.app;

import com.github.nexus.config.ConfigHolder;
import com.github.nexus.config.Configuration;
import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import com.github.nexus.service.locator.ServiceLocator;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static java.util.Collections.singletonList;

/**
 * The main entry point for the application.
 * This just starts up the application in the embedded container.
 */
public class Launcher {

    public static final URI SERVER_URI = UriBuilder.fromUri("http://0.0.0.0/").port(8080).build();

    public static void main(final String... args) throws Exception {

        final Configuration configuration = new Configuration();
        ConfigHolder.INSTANCE.setConfiguration(configuration);
        configuration.setPublicKeys(singletonList("key.pub"));
        configuration.setPrivateKeys(singletonList("key.key"));

        final Nexus nexus = new Nexus(ServiceLocator.create());

        final RestServer restServer = RestServerFactory.create().createServer(SERVER_URI, nexus);
        
        restServer.start();
        
        System.in.read();
        
        restServer.stop();

        System.exit(0); 
    }

}
