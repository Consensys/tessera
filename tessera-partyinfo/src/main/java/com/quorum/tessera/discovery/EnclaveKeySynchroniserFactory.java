package com.quorum.tessera.discovery;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveKeySynchroniserFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveKeySynchroniserFactory.class);


    public static EnclaveKeySynchroniser provider() {
        LOGGER.info("Creating Enclave");
        Enclave enclave = EnclaveFactory.create().enclave().get();
        LOGGER.info("Created Enclave {}",enclave);
        LOGGER.info("Creating NetworkStore");
        NetworkStore networkStore = NetworkStore.getInstance();
        LOGGER.info("Created NetworkStore");
        return new EnclaveKeySynchroniserImpl(enclave, networkStore);
    }

}
