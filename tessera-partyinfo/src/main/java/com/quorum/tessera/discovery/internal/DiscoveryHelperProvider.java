package com.quorum.tessera.discovery.internal;

import com.quorum.tessera.discovery.DiscoveryHelper;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.enclave.Enclave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryHelperProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryHelperProvider.class);

  public static DiscoveryHelper provider() {
    LOGGER.debug("Creating network store");
    final NetworkStore networkStore = NetworkStore.getInstance();
    LOGGER.debug("Created network store {}", networkStore);

    LOGGER.debug("Creating enclave");
    Enclave enclave = Enclave.create();
    LOGGER.debug("Created enclave {}", enclave);

    return new DiscoveryHelperImpl(networkStore, enclave);
  }
}
