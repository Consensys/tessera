package com.quorum.tessera.enclave;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.EncryptorConfig;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.config.keys.KeyEncryptor;
import com.quorum.tessera.config.keys.KeyEncryptorFactory;
import com.quorum.tessera.config.util.EnvironmentVariableProvider;
import com.quorum.tessera.config.util.KeyDataUtil;
import com.quorum.tessera.encryption.*;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveFactoryImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveFactoryImpl.class);

  private final Config config;

  public EnclaveFactoryImpl(Config config) {
    this.config = Objects.requireNonNull(config);
  }

  public Enclave createLocal() {
    return createServer(config);
  }

  public Enclave createEnclave() {

    LOGGER.info("Creating enclave");
    try {
      final Optional<ServerConfig> enclaveServerConfig =
          config.getServerConfigs().stream().filter(sc -> sc.getApp() == AppType.ENCLAVE).findAny();

      if (enclaveServerConfig.isPresent()) {
        LOGGER.info("Creating remoted enclave");
        return EnclaveClient.create();
      }
      return createServer(config);
    } catch (Throwable ex) {
      LOGGER.error("", ex);
      throw ex;
    }
  }

  static Enclave createServer(Config config) {

    LOGGER.info("Creating enclave server");

    EncryptorConfig encryptorConfig = config.getEncryptor();
    EncryptorFactory encryptorFactory =
        EncryptorFactory.newFactory(encryptorConfig.getType().name());
    Encryptor encryptor = encryptorFactory.create(encryptorConfig.getProperties());
    KeyEncryptor keyEncryptor = KeyEncryptorFactory.newFactory().create(encryptorConfig);

    final KeyPairConverter keyPairConverter =
        new KeyPairConverter(config, new EnvironmentVariableProvider());
    final Collection<KeyPair> keys =
        keyPairConverter.convert(
            config.getKeys().getKeyData().stream()
                .map(kd -> KeyDataUtil.unmarshal(kd, keyEncryptor))
                .collect(Collectors.toList()));

    final Collection<PublicKey> forwardKeys = keyPairConverter.convert(config.getAlwaysSendTo());

    LOGGER.debug("Creating enclave");

    Enclave enclave = new EnclaveImpl(encryptor, new KeyManagerImpl(keys, forwardKeys));

    LOGGER.debug("Created enclave {}", enclave);

    return enclave;
  }
}
