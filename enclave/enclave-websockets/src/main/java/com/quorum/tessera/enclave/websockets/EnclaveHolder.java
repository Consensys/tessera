package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

public enum EnclaveHolder {
    
    INSTANCE;
    
   private EnclaveFactory enclaveFactory;
   
   private Config config;
    

    public static EnclaveHolder instance(EnclaveFactory enclaveFactory, Config config) {

        INSTANCE.enclaveFactory = enclaveFactory;
        INSTANCE.config = config;

        return INSTANCE;
    }
 
    public Enclave getEnclave() {
        return enclaveFactory.createLocal(config);
    }
    
}
