package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.cli.CliResult;
import com.quorum.tessera.cli.parsers.ConfigConverter;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveServer;
import com.quorum.tessera.enclave.server.EnclaveCliAdapter;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import java.security.Security;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String... args) throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    final CommandLine commandLine = new CommandLine(new EnclaveCliAdapter());
    commandLine
        .registerConverter(Config.class, new ConfigConverter())
        .setSeparator(" ")
        .setCaseInsensitiveEnumValuesAllowed(true);

    commandLine.execute(args);
    final CliResult cliResult = commandLine.getExecutionResult();

    if (cliResult == null) {
      System.exit(1);
    }

    if (!cliResult.getConfig().isPresent()) {
      System.exit(cliResult.getStatus());
    }

    final TesseraServerFactory restServerFactory =
        TesseraServerFactory.create(CommunicationType.REST);

    final Config config = cliResult.getConfig().get();

    ConfigFactory.create().store(config);

    final ServerConfig serverConfig = config.getServerConfigs().stream().findFirst().get();
    Enclave enclave = EnclaveServer.create();
    LOGGER.debug("Created enclave {}", enclave);
    final TesseraServer server =
        restServerFactory.createServer(serverConfig, Set.of(new EnclaveApplication(enclave)));
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
