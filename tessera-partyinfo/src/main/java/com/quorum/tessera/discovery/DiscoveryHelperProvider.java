package com.quorum.tessera.discovery;

import com.quorum.tessera.enclave.Enclave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryHelperProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryHelperProvider.class);

    public static DiscoveryHelper provider() {
        LOGGER.info("Creating network store");
        final NetworkStore networkStore = NetworkStore.getInstance();
        LOGGER.info("Created network store {}",networkStore);

        LOGGER.info("Creating enclave");
        Enclave enclave = Enclave.create();
        LOGGER.info("Created enclave {}",enclave);

        return new DiscoveryHelperImpl(networkStore, enclave);
    }


}
