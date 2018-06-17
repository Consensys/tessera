package com.github.nexus;

import com.github.nexus.api.Nexus;
import com.github.nexus.configuration.Configuration;
import com.github.nexus.configuration.ConfigurationParser;
import com.github.nexus.configuration.PropertyLoader;
import com.github.nexus.keygen.KeyGenerator;
import com.github.nexus.keygen.KeyGeneratorFactory;
import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import com.github.nexus.service.locator.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.io.OutputStream;
import java.lang.management.ManagementFactory;

import com.github.nexus.socket.HttpProxyFactory;
import com.github.nexus.socket.SocketServer;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

/**
 * The main entry point for the application. This just starts up the application
 * in the embedded container.
 */
public class Launcher {

    public static List<String> cliArgumentList = Collections.emptyList();

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(final String... args) throws Exception {

        Launcher.createPidFile();

        Launcher.cliArgumentList = Arrays.asList(args);
        final Configuration config = ConfigurationParser
            .create()
            .config(PropertyLoader.create(), cliArgumentList);

        if(config.generatekeys().isEmpty()) {
            //no keys to generate

            // Start a listener on the unix domain socket, that attaches to the HTTP server
            final SocketServer socketServer = new SocketServer(config, new HttpProxyFactory(), config.uri());

            runWebServer(config.uri());
        } else {
            //keys to generate
            final KeyGenerator keyGenerator = KeyGeneratorFactory.create(config);
            config.generatekeys().forEach(name -> keyGenerator.promptForGeneration(name, System.in));
        }

        System.exit(0);
    }

    private static void runWebServer(final URI serverUri) throws Exception {
        final Nexus nexus = new Nexus(ServiceLocator.create(), "nexus-spring.xml");

        final RestServer restServer = RestServerFactory.create().createServer(serverUri, nexus);

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
