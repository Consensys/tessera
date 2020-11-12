package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveProvider.class);

    public static Enclave provider() {
        EnclaveFactory enclaveFactory = EnclaveFactory.create();
        LOGGER.debug("Created EnclaveFactory {}",enclaveFactory);
        if(enclaveFactory.enclave().isPresent()) {
            return enclaveFactory.enclave().get();
        }

        Config config = ConfigFactory.create().getConfig();

        LOGGER.debug("Found config {}",config);

        return enclaveFactory.create(config);

    }

}
