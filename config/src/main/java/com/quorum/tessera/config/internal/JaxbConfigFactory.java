package com.quorum.tessera.config.internal;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.JaxbUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JaxbConfigFactory implements ConfigFactory {

  private final KeyEncryptorFactory keyEncryptorFactory;

  private static final EncryptorConfig DEFAULT_ENCRYPTOR_CONFIG = EncryptorConfig.getDefault();

  protected JaxbConfigFactory(KeyEncryptorFactory keyEncryptorFactory) {
    this.keyEncryptorFactory = keyEncryptorFactory;
  }

  @Override
  public Config create(final InputStream configData) {

    byte[] originalData =
        Stream.of(configData)
            .map(InputStreamReader::new)
            .map(BufferedReader::new)
            .flatMap(BufferedReader::lines)
            .collect(Collectors.joining(System.lineSeparator()))
            .getBytes();

    final Config initialConfig =
        JaxbUtil.unmarshal(new ByteArrayInputStream(originalData), Config.class);

    EncryptorConfig encryptorConfig =
        Optional.ofNullable(initialConfig.getEncryptor()).orElse(DEFAULT_ENCRYPTOR_CONFIG);
    // Initialise the key encrypter it will store into holder object.
    keyEncryptorFactory.create(encryptorConfig);

    final Config config = JaxbUtil.unmarshal(new ByteArrayInputStream(originalData), Config.class);
    config.setEncryptor(encryptorConfig);

    return config;
  }

  @Override
  public void store(Config config) {
    ConfigHolder.INSTANCE.setConfig(config);
  }
}
