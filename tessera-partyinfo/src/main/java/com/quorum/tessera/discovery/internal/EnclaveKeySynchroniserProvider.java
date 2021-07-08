package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.discovery.EnclaveKeySynchroniser;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.enclave.Enclave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveKeySynchroniserProvider {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(EnclaveKeySynchroniserProvider.class);

  public static EnclaveKeySynchroniser provider() {
    LOGGER.debug("Creating Enclave");
    Enclave enclave = Enclave.create();
    LOGGER.debug("Created Enclave {}", enclave);
    LOGGER.debug("Creating NetworkStore");
    NetworkStore networkStore = NetworkStore.getInstance();
    LOGGER.debug("Created NetworkStore");
    return new EnclaveKeySynchroniserImpl(enclave, networkStore);
  }
}
