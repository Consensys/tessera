package com.github.nexus;

import com.github.nexus.api.Nexus;
import com.github.nexus.config.Config;
import com.github.nexus.config.ServerConfig;
import com.github.nexus.config.cli.CliDelegate;
import com.github.nexus.config.cli.CliResult;
import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import com.github.nexus.service.locator.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * The main entry point for the application. This just starts up the application
 * in the embedded container.
 */
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(final String... args) throws Exception {

        try {
            CliResult cliResult = CliDelegate.instance().execute(args);

            if(cliResult.isHelpOn()) {
                System.exit(0);
            } else if(cliResult.getStatus() != 0) {
                System.exit(cliResult.getStatus());
            }

            Config config = cliResult.getConfig().get();

            final URI uri = new URI(config.getServerConfig().getHostName() + ":" + config.getServerConfig().getPort());

            runWebServer(uri, config.getServerConfig());

            System.exit(0);

        } catch(ConstraintViolationException ex) {
            Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

            for(ConstraintViolation<?> violation : violations) {
                System.out.println("Config validation issue: " + violation.getPropertyPath() + " " + violation.getMessage());
            }
            System.exit(1);
        }
    }

    private static void runWebServer(final URI serverUri, ServerConfig serverConfig) throws Exception {

        final Nexus nexus = new Nexus(ServiceLocator.create(), "nexus-spring.xml");

        final RestServer restServer = RestServerFactory.create().createServer(serverUri, nexus, serverConfig);

        CountDownLatch countDown = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                restServer.stop();
            } catch (Exception ex) {
                LOGGER.error(null, ex);
            } finally {
                countDown.countDown();
            }
        }));

        restServer.start();

        countDown.await();
    }

}
