package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveServerProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveServerProvider.class);

  public static EnclaveServer provider() {
    Config config = ConfigFactory.create().getConfig();
    Enclave enclave = EnclaveFactoryImpl.createServer(config);
    LOGGER.debug("Creating server with {}", enclave);
    return new EnclaveServerImpl(enclave);
  }
}
