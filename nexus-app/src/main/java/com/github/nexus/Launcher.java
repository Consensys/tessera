package com.github.nexus;

import com.github.nexus.api.Nexus;
import com.github.nexus.config.Config;
import com.github.nexus.config.SslConfig;
import com.github.nexus.config.cli.CliDelegate;
import com.github.nexus.server.RestServer;
import com.github.nexus.server.RestServerFactory;
import com.github.nexus.service.locator.ServiceLocator;
import com.github.nexus.ssl.strategy.TrustMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import java.util.stream.Stream;

/**
 * The main entry point for the application. This just starts up the application
 * in the embedded container.
 */
public class Launcher {


    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(final String... args) throws Exception {
        
        Config config = CliDelegate.instance().execute(args);
        
        Launcher.createPidFile();

         final URI uri = new URI(config.getServerConfig().getHostName() + ":"+ config.getServerConfig().getPort());
        
        if(Objects.nonNull(config.getServerConfig().getSslConfig())) {
            
            SslConfig sslConfig = config.getServerConfig().getSslConfig();

                TrustMode trustMode = Stream.of(TrustMode.values())
                        .filter(tm -> Objects.equals(tm.name(), sslConfig.getServerTrustMode().name()))
                        .findAny().orElse(TrustMode.NONE);
                
                String keyStore = sslConfig.getServerKeyStore().toString();
                String keyStorePassword = sslConfig.getServerKeyStorePassword();
                String trustStore = sslConfig.getServerTrustStore().toString();
                String trustStorePassword = sslConfig.getServerTrustStorePassword();
                String knownHostsFile = sslConfig.getKnownServersFile().toString();
                
                final SSLContext sslContext = trustMode
                        .createSSLContext(keyStore, keyStorePassword, trustStore, trustStorePassword, knownHostsFile);

                runWebServer(uri, sslContext, true);

        } else {
             runWebServer(uri, SSLContext.getDefault(), false);
        }

        System.exit(0);
    }

    private static void runWebServer(final URI serverUri, SSLContext sslContext, boolean secure) throws Exception {

        final Nexus nexus = new Nexus(ServiceLocator.create(), "nexus-spring.xml");

        final RestServer restServer = RestServerFactory.create().createServer(serverUri, nexus, sslContext, secure);

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
