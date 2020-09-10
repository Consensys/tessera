package com.quorum.tessera.launcher;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.apps.TesseraAppFactory;
import com.quorum.tessera.recovery.RecoveryFactory;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public enum Launcher {
    NORMAL {
        @Override
        public void launchServer(Config config) throws Exception {
            LOGGER.debug("Creating servers");
            final List<TesseraServer> servers =
                    config.getServerConfigs().stream()
                            .filter(server -> !AppType.ENCLAVE.equals(server.getApp()))
                            .map(
                                    conf -> {
                                        LOGGER.debug("Creating app from {}", conf);
                                        Object app =
                                                TesseraAppFactory.create(conf.getCommunicationType(), conf.getApp())
                                                        .orElseThrow(
                                                                () ->
                                                                        new IllegalStateException(
                                                                                "Cant create app for "
                                                                                        + conf.getApp()));
                                        LOGGER.debug("Created APP {} from {}", app, conf);
                                        return TesseraServerFactory.create(conf.getCommunicationType())
                                                .createServer(conf, Collections.singleton(app));
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
                    TesseraAppFactory.create(recoveryP2PServer.getCommunicationType(), recoveryP2PServer.getApp())
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

            final int exitCode = RecoveryFactory.newFactory().create(config).recover();

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
