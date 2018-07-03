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
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * The main entry point for the application. This just starts up the application
 * in the embedded container.
 */
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(final String... args) throws Exception {

        try {
            CliResult cliResult = CliDelegate.instance().execute(args);

            if(cliResult.getStatus() != 0) {
                System.exit(cliResult.getStatus());
            }

            Config config = cliResult.getConfig().get();

            Launcher.createPidFile();

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

    private static void createPidFile() throws IOException {

        final String pidFilePath = System.getProperty("nexus.pid.file", null);
        if (Objects.nonNull(pidFilePath)) {

            final String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

            final Path filePath = Paths.get(pidFilePath);
            if (Files.exists(filePath)) {
                LOGGER.info("File already exists {}", filePath);
            } else {
                Files.createFile(filePath);
                LOGGER.info("Creating pid file {}", filePath);
            }

            try (final OutputStream stream = Files.newOutputStream(filePath, CREATE, TRUNCATE_EXISTING)) {
                stream.write(pid.getBytes(StandardCharsets.UTF_8));
            }
        }

    }

}
