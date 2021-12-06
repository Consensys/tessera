package com.quorum.tessera.launcher;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.recovery.Recovery;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Launcher {
  NORMAL {
    @Override
    public void launchServer(Config config) throws Exception {
      LOGGER.debug("Creating servers");
      config
          .getServerConfigs()
          .forEach(
              c -> {
                LOGGER.debug("Creating server for {}", c);
              });

      final List<TesseraServer> servers =
          config.getServerConfigs().stream()
              .filter(server -> !AppType.ENCLAVE.equals(server.getApp()))
              .map(
                  conf -> {
                    LOGGER.debug("Creating app from {}", conf);

                    ServiceLoader.load(TesseraApp.class).stream()
                        .forEach(
                            app -> {
                              LOGGER.debug("Loaded app {}", app.type());
                            });

                    Object app =
                        ServiceLoader.load(TesseraApp.class).stream()
                            .map(p -> p.get())
                            .filter(a -> a.getAppType() == conf.getApp())
                            .filter(a -> a.getCommunicationType() == conf.getCommunicationType())
                            .findFirst()
                            .orElseThrow(
                                () ->
                                    new IllegalStateException(
                                        "Cant create app for " + conf.getApp()));

                    LOGGER.debug("Created APP {} from {}", app, conf);
                    return TesseraServerFactory.create(conf.getCommunicationType())
                        .createServer(conf, Set.of(app));
                  })
              .filter(Objects::nonNull)
              .collect(Collectors.toList());

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      for (TesseraServer ts : servers) {
                        ts.stop();
                      }
                    } catch (Exception ex) {
                      LOGGER.error(null, ex);
                    }
                  }));

      for (TesseraServer ts : servers) {
        LOGGER.debug("Starting server {}", ts);
        ts.start();
        LOGGER.debug("Started server {}", ts);
      }
      LOGGER.debug("Created servers");

      if (config.outputServerURIs()) {
        // Write server URIs to file
        final List<String> uriPaths = writeServerURIs(config, servers);

        // Add a shutdown hook to clean them up
        Runtime.getRuntime()
            .addShutdownHook(
                new Thread(
                    () -> {
                      try {
                        for (final String uriFilePath : uriPaths) {
                          Files.delete(Path.of(uriFilePath));
                        }
                      } catch (Exception ex) {
                        LOGGER.error(null, ex);
                      }
                    }));
      }
    }
  },

  RECOVERY {
    @Override
    public void launchServer(Config config) throws Exception {

      final ServerConfig recoveryP2PServer = config.getP2PServerConfig();

      final Object app =
          ServiceLoader.load(TesseraApp.class).stream()
              .map(a -> a.get())
              .peek(o -> LOGGER.debug("Found app {}", o))
              .filter(a -> a.getCommunicationType() == recoveryP2PServer.getCommunicationType())
              .filter(a -> a.getAppType() == recoveryP2PServer.getApp())
              .findFirst()
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Cant create app for " + recoveryP2PServer.getApp()));

      final TesseraServer recoveryServer =
          TesseraServerFactory.create(recoveryP2PServer.getCommunicationType())
              .createServer(recoveryP2PServer, Collections.singleton(app));

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      recoveryServer.stop();
                    } catch (Exception ex) {
                      LOGGER.error(null, ex);
                    }
                  }));

      LOGGER.debug("Starting recovery server");
      recoveryServer.start();
      LOGGER.debug("Started recovery server");

      LOGGER.info("Waiting for nodes to synchronise with peers");
      Thread.sleep(10000);

      final int exitCode = Recovery.create().recover();

      System.exit(exitCode);
    }
  };

  private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

  public abstract void launchServer(Config config) throws Exception;

  public static Launcher create(final boolean isRecoveryMode) {
    if (isRecoveryMode) {
      return Launcher.RECOVERY;
    }
    return Launcher.NORMAL;
  }

  private static List<String> writeServerURIs(
      final Config config, final List<TesseraServer> servers) throws IOException {
    final TesseraServer q2tServer =
        servers.stream()
            .filter(server -> server.getAppType() != null && server.getAppType() == AppType.Q2T)
            .findFirst()
            .orElse(null);

    final TesseraServer p2pServer =
        servers.stream()
            .filter(server -> server.getAppType() != null && server.getAppType() == AppType.P2P)
            .findFirst()
            .orElse(null);

    final TesseraServer thirdPartyServer =
        servers.stream()
            .filter(
                server -> server.getAppType() != null && server.getAppType() == AppType.THIRD_PARTY)
            .findFirst()
            .orElse(null);

    final Path path = Paths.get(config.outputServerURIPath());
    final List<String> uriPaths = new LinkedList<>();

    uriPaths.add(Launcher.writeURIFile(path, q2tServer, "q2tServer.uri"));
    uriPaths.add(Launcher.writeURIFile(path, p2pServer, "p2pServer.uri"));
    uriPaths.add(Launcher.writeURIFile(path, thirdPartyServer, "thirdPartyServer.uri"));

    return uriPaths;
  }

  private static String writeURIFile(
      final Path dirPath, final TesseraServer tesseraServer, final String outputFile)
      throws IOException {
    final Path path =
        Files.writeString(
            Path.of(dirPath.toAbsolutePath() + "/" + outputFile),
            tesseraServer.getUri().toString());
    return path.toString();
  }
}
