package com.quorum.tessera.config.util;

import com.quorum.tessera.io.SystemAdapter;
import com.quorum.tessera.passwords.PasswordReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class ConfigSecretReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSecretReader.class);

    private ConfigSecretReader() {}

    public static Optional<char[]> readSecretFromFile() {

        final EnvironmentVariableProvider envProvider = new EnvironmentVariableProvider();

        if (envProvider.hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH)) {
            final Path secretPath = Paths.get(envProvider.getEnv(EnvironmentVariables.CONFIG_SECRET_PATH));
            if (Files.exists(secretPath)) {
                try {
                    return Optional.of(new String(Files.readAllBytes(secretPath)).trim().toCharArray());
                } catch (IOException ex) {
                    LOGGER.error("Error while reading secret from file");
                }
            }
        }

        LOGGER.warn("Not able to find or read any secret for decrypting sensitive values in config.");

        return Optional.empty();
    }

    public static char[] readSecretFromConsole() {
        SystemAdapter.INSTANCE.out().println("Please enter the secret/password used to decrypt config value");
        return PasswordReaderFactory.create().readPasswordFromConsole();
    }
}
