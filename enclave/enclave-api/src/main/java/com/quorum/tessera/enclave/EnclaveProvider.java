package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveProvider.class);

  public static Enclave provider() {
    EnclaveHolder enclaveHolder = DefaultEnclaveHolder.INSTANCE;
    if (enclaveHolder.getEnclave().isPresent()) {
      return enclaveHolder.getEnclave().get();
    }

    Config config = ConfigFactory.create().getConfig();

    EnclaveFactoryImpl enclaveFactory = new EnclaveFactoryImpl(config);

    LOGGER.debug("Found config {}", config);

    Enclave enclave = enclaveFactory.createEnclave();

    return enclaveHolder.setEnclave(enclave);
  }
}
