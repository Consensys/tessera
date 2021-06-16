package com.quorum.tessera.launcher;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.apps.TesseraApp;
import com.quorum.tessera.recovery.Recovery;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
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

  public static Launcher create(boolean isRecoveryMode) {
    if (isRecoveryMode) {
      return Launcher.RECOVERY;
    }
    return Launcher.NORMAL;
  }
}
