package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.server.EnclaveCliAdapter;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) throws Exception {
        System.setProperty("javax.xml.bind.JAXBContextFactory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        final CommandLine commandLine = new CommandLine(new EnclaveCliAdapter());
        commandLine
                .registerConverter(Config.class, new ConfigConverter())
                .setSeparator(" ")
                .setCaseInsensitiveEnumValuesAllowed(true);

        commandLine.execute(args);
        final CliResult cliResult = commandLine.getExecutionResult();

        if (!cliResult.getConfig().isPresent()) {
            System.exit(cliResult.getStatus());
        }

        TesseraServerFactory restServerFactory = TesseraServerFactory.create(CommunicationType.REST);

        Config config = cliResult.getConfig().get();

        Enclave enclave = EnclaveFactory.createServer(config);

        EnclaveResource enclaveResource = new EnclaveResource(enclave);

        final EnclaveApplication application = new EnclaveApplication(enclaveResource);

        final ServerConfig serverConfig = config.getServerConfigs().iterator().next();

        TesseraServer server = restServerFactory.createServer(serverConfig, Collections.singleton(application));

        server.start();

        CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        server.stop();
                                    } catch (Exception ex) {
                                        LOGGER.error(null, ex);
                                    } finally {

                                    }
                                }));

        latch.await();
    }
}
