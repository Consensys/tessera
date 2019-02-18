package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import java.util.ServiceLoader;


public interface EnclaveClientFactory {

    EnclaveClient create(Config config);
    
    static EnclaveClientFactory create() {
        return ServiceLoader.load(EnclaveClientFactory.class)
                .iterator().next();
    }
    
}
