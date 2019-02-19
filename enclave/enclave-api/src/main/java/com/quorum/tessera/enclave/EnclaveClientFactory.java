package com.quorum.tessera.enclave;

import com.quorum.tessera.config.Config;
import java.util.ServiceLoader;


public interface EnclaveClientFactory<T extends EnclaveClient> {

    T create(Config config);
    
    static EnclaveClientFactory create() {
        return ServiceLoader.load(EnclaveClientFactory.class)
                .iterator().next();
    }
    
}
