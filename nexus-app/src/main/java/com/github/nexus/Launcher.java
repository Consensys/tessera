package com.github.nexus;

import com.github.nexus.api.Nexus;
import com.github.nexus.configuration.ConfigurationFactory;
import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import com.github.nexus.service.locator.ServiceLocator;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main entry point for the application.
 * This just starts up the application in the embedded container.
 */
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);
    
    public static final URI SERVER_URI = UriBuilder.fromUri("http://0.0.0.0/").port(8080).build();

    public static void main(final String... args) throws Exception {

        ConfigurationFactory.cliArgsArray = args;

        final Nexus nexus = new Nexus(ServiceLocator.create());

        final RestServer restServer = RestServerFactory.create().createServer(SERVER_URI, nexus);
        
        CountDownLatch countDown = new CountDownLatch(1);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    restServer.stop();
                } catch (Exception ex) {
                    LOGGER.error(null,ex);
                } finally {
                    countDown.countDown();
                }
            }
        });
        
        restServer.start();

        countDown.await();
        
        System.exit(0); 
    }

}
