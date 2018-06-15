package com.github.nexus;


import com.github.nexus.api.Nexus;
import com.github.nexus.configuration.ConfigurationParser;
import com.github.nexus.configuration.PropertyLoader;
import com.github.nexus.configuration.Configuration;
import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import com.github.nexus.service.locator.ServiceLocator;
import com.github.nexus.socket.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * The main entry point for the application.
 * This just starts up the application in the embedded container.
 */
public class Launcher {

    public static List<String> cliArgumentList = Collections.emptyList();

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static final URI SERVER_URI = UriBuilder.fromUri("http://0.0.0.0/").port(8080).build();


    public static void main(final String... args) throws Exception {

        Launcher.cliArgumentList = Arrays.asList(args);
        final Configuration config = ConfigurationParser.create().config(PropertyLoader.create(), cliArgumentList);

        final URI serverUri = UriBuilder
            .fromUri(config.url())
            .port(config.port())
            .build();

        final Nexus nexus = new Nexus(ServiceLocator.create());

        final RestServer restServer = RestServerFactory.create().createServer(serverUri, nexus);

        final SocketServer socketServer = new SocketServer();

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
