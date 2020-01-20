package com.quorum.tessera.config.util;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyVaultConfig;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.io.FilesDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class ConfigFileUpdaterWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileUpdaterWriter.class);

    private final FilesDelegate filesDelegate;

    public ConfigFileUpdaterWriter(FilesDelegate filesDelegate) {
        this.filesDelegate = filesDelegate;
    }

    public void updateAndWrite(
            List<ConfigKeyPair> newKeys, KeyVaultConfig keyVaultConfig, Config config, Path configDest)
            throws IOException {
        LOGGER.info("Writing updated config to {}", configDest);

        config.getKeys().getKeyData().addAll(newKeys);
        if (Optional.ofNullable(keyVaultConfig).isPresent()
                && !Optional.ofNullable(config)
                        .map(Config::getKeys)
                        .flatMap(k -> k.getKeyVaultConfig(keyVaultConfig.getKeyVaultType()))
                        .isPresent()) {
            config.getKeys().addKeyVaultConfig(keyVaultConfig);
        }

        try (OutputStream out = filesDelegate.newOutputStream(configDest, CREATE_NEW)) {
            JaxbUtil.marshal(config, out);
            LOGGER.info("Updated config written to {}", configDest);
        } catch (Exception e) {
            LOGGER.info("Writing updated config failed, cleaning up: deleting {}", configDest);
            filesDelegate.deleteIfExists(configDest);

            throw e;
        }
    }
}
