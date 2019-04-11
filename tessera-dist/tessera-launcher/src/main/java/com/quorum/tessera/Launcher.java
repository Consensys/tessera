package com.quorum.tessera;

import com.quorum.tessera.config.*;
import com.quorum.tessera.config.cli.CliDelegate;
import com.quorum.tessera.config.cli.CliException;
import com.quorum.tessera.config.cli.CliResult;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import com.quorum.tessera.service.locator.ServiceLocator;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The main entry point for the application. This just starts up the application
 * in the embedded container.
 */
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(final String... args) throws Exception {

        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        try {
            final CliResult cliResult = CliDelegate.instance().execute(args);

            if (cliResult.isSuppressStartup()) {
                System.exit(0);
            }

            if (cliResult.getStatus() != 0) {
                System.exit(cliResult.getStatus());
            }

            final Config config = cliResult.getConfig()
                .orElseThrow(() -> new NoSuchElementException("No config found. Tessera will not run."));

            runWebServer(config);

        } catch (final ConstraintViolationException ex) {
            for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                System.out.println("Config validation issue: " + violation.getPropertyPath() + " " + violation.getMessage());
            }
            System.exit(1);
        } catch (final ConfigException ex) {
            final Throwable cause = ExceptionUtils.getRootCause(ex);

            if (JsonException.class.isInstance(cause)) {
                System.err.println("Invalid json, error is " + cause.getMessage());
            } else {
                System.err.println(Objects.toString(cause));
            }
            System.exit(3);
        } catch (final CliException ex) {
            System.err.println(ex.getMessage());
            System.exit(4);
        } catch (final Throwable ex) {
            Optional.ofNullable(ex.getMessage()).ifPresent(System.err::println);
            System.exit(2);
        }

    }

    private static void runWebServer(final Config config) throws Exception {

        ServiceLocator serviceLocator = ServiceLocator.create();

        Set<Object> services = serviceLocator.getServices("tessera-spring.xml");

        final List<TesseraServer> servers = config.getServerConfigs()
            .stream()
            .filter(server -> !AppType.ENCLAVE.equals(server.getApp()))
            .map(conf -> TesseraServerFactory.create(conf.getCommunicationType()).createServer(conf, services))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                for (TesseraServer ts : servers) {
                    ts.stop();
                }
            } catch (Exception ex) {
                LOGGER.error(null, ex);
            }
        }));

        for (TesseraServer ts : servers) {
            ts.start();
        }

    }

}
