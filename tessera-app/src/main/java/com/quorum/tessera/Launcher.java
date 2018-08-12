package com.quorum.tessera;

import com.quorum.tessera.api.Tessera;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.cli.CliDelegate;
import com.quorum.tessera.config.cli.CliResult;
import com.quorum.tessera.server.RestServer;
import com.quorum.tessera.server.RestServerFactory;
import com.quorum.tessera.service.locator.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import javax.json.JsonException;

/**
 * The main entry point for the application. This just starts up the application
 * in the embedded container.
 */
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(final String... args) throws Exception {

        try {
            final CliResult cliResult = CliDelegate.instance().execute(args);

            if (cliResult.isHelpOn()) {
                System.exit(0);
            } else if (cliResult.getStatus() != 0) {
                System.exit(cliResult.getStatus());
            }

            if (!cliResult.getConfig().isPresent() && cliResult.isKeyGenOn()) {
                System.exit(cliResult.getStatus());
            }

            final Config config = cliResult.getConfig()
                    .orElseThrow(() -> new NoSuchElementException("No Config found. Tessera will not run"));

            final URI uri = new URI(config.getServerConfig().getHostName() + ":" + config.getServerConfig().getPort());

            runWebServer(uri, config.getServerConfig());

            System.exit(0);

        } catch (final ConstraintViolationException ex) {
            Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

            for (ConstraintViolation<?> violation : violations) {
                System.out.println("Config validation issue: " + violation.getPropertyPath() + " " + violation.getMessage());
            }
            System.exit(1);
        } catch (com.quorum.tessera.config.ConfigException ex) {
            Throwable cause = resolveRooCause(ex);

            if (JsonException.class.isInstance(cause)) {
                System.err.println("Invalid json, error is " + cause.getMessage());
            } else {
                System.err.println(Objects.toString(cause));
            }
            System.exit(3);
        } catch (com.quorum.tessera.config.cli.CliException ex) {
            System.err.println(ex.getMessage());
            System.exit(4);
        } catch (Throwable ex) {

            Optional.ofNullable(ex.getMessage()).ifPresent(System.err::println);
            System.exit(2);
        }
    }

    static Throwable resolveRooCause(Throwable ex) {
        if (Objects.nonNull(ex.getCause())) {
            return resolveRooCause(ex.getCause());
        }
        return ex;
    }

    private static void runWebServer(final URI serverUri, ServerConfig serverConfig) throws Exception {

        final Tessera tessera = new Tessera(ServiceLocator.create(), "tessera-spring.xml");

        final RestServer restServer = RestServerFactory.create().createServer(serverUri, tessera, serverConfig);

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
