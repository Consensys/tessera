package com.quorum.tessera.config.util;

import com.quorum.tessera.passwords.PasswordReaderFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigSecretReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSecretReader.class);

  private final EnvironmentVariableProvider environmentVariableProvider;

  public ConfigSecretReader(EnvironmentVariableProvider environmentVariableProvider) {
    this.environmentVariableProvider = environmentVariableProvider;
  }

  public Optional<char[]> readSecretFromFile() {

    if (environmentVariableProvider.hasEnv(EnvironmentVariables.CONFIG_SECRET_PATH)) {
      final Path secretPath =
          Paths.get(environmentVariableProvider.getEnv(EnvironmentVariables.CONFIG_SECRET_PATH));
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

  public char[] readSecretFromConsole() {
    System.out.println("Please enter the secret/password used to decrypt config value");
    return PasswordReaderFactory.create().readPasswordFromConsole();
  }
}
