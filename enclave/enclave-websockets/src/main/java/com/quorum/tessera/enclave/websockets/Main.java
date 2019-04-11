package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.cli.CliDelegate;
import com.quorum.tessera.config.cli.CliResult;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import com.quorum.tessera.service.locator.ServiceLocator;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws Exception {

        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        CliResult cliResult = CliDelegate.INSTANCE.execute(args);

        if(0 != cliResult.getStatus()) {
            System.err.println("Error starting server");
            System.exit(cliResult.getStatus());
        }
        
        ServiceLocator serviceLocator = ServiceLocator.create();
        Set<Object> services = serviceLocator.getServices("tessera-enclave-websocket-spring.xml");

        Config config = services.stream()
                .filter(Config.class::isInstance)
                .map(Config.class::cast)
                .findAny().get();

        TesseraServerFactory serverFactory = TesseraServerFactory.create(CommunicationType.WEB_SOCKET);

        ServerConfig serverConfig = config.getServerConfigs().stream()
                .filter(s -> s.getApp() == AppType.ENCLAVE)
                .filter(s -> s.getCommunicationType() == CommunicationType.WEB_SOCKET)
                .findAny().get();

        TesseraServer server = serverFactory.createServer(serverConfig, Collections.singleton(EnclaveEndpoint.class));
        server.start();

        CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            try{
                server.stop();
            } catch (Exception ex) {
                LOGGER.error(null, ex);
            } finally {

            }
        }));

        latch.await();
    }
}
