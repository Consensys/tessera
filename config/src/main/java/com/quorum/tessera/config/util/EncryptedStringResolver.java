package com.quorum.tessera.config.util;

import java.util.Objects;
import org.jasypt.encryption.pbe.PBEStringCleanablePasswordEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedStringResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedStringResolver.class);

  private final PBEStringCleanablePasswordEncryptor encryptor;

  private boolean isPasswordSet;

  private final ConfigSecretReader configSecretReader;

  protected EncryptedStringResolver(
      ConfigSecretReader configSecretReader, PBEStringCleanablePasswordEncryptor encryptor) {
    this.configSecretReader = Objects.requireNonNull(configSecretReader);
    this.encryptor = Objects.requireNonNull(encryptor);
  }

  public EncryptedStringResolver() {
    this(
        new ConfigSecretReader(new EnvironmentVariableProvider()),
        new StandardPBEStringEncryptor());
  }

  public String resolve(final String textToDecrypt) {

    if (PropertyValueEncryptionUtils.isEncryptedValue(textToDecrypt)) {

      if (!isPasswordSet) {
        encryptor.setPasswordCharArray(
            configSecretReader
                .readSecretFromFile()
                .orElseGet(configSecretReader::readSecretFromConsole));
        isPasswordSet = true;
      }

      return PropertyValueEncryptionUtils.decrypt(textToDecrypt, encryptor);
    }

    LOGGER.warn(
        "Some sensitive values are being given as unencrypted plain text in config. "
            + "Please note this is NOT recommended for production environment.");

    return textToDecrypt;
  }
}
