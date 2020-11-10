package com.quorum.tessera.discovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;

public class DiscoveryHelperFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryHelperFactory.class);

    public static DiscoveryHelper provider() {
        LOGGER.info("Creating network store");
        final NetworkStore networkStore = ServiceLoader.load(NetworkStore.class).findFirst().get();
        LOGGER.info("Created network store");

        LOGGER.info("Creating enclave");

        Enclave enclave = EnclaveFactory.create().enclave().get();

        LOGGER.info("Created enclave");

        return new DiscoveryHelperImpl(networkStore, enclave);
    }


}
