package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.Enclave;
import java.util.ServiceLoader;


public interface EnclaveFactory {
    
    Enclave create();
    
    static EnclaveFactory newFactory() {
        
        return ServiceLoader.load(EnclaveFactory.class).iterator().next();
        
    } 
    
}
