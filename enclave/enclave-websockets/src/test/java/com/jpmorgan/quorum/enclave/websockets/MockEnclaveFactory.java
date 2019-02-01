package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.enclave.Enclave;
import static org.mockito.Mockito.mock;


public class MockEnclaveFactory implements EnclaveFactory {

    public static final Enclave ENCLAVE = mock(Enclave.class);
    
    @Override
    public Enclave create() {
        return ENCLAVE;
    }
    
    
}
