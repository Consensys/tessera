package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.enclave.Enclave;


public enum EnclaveHolder {
    
    INSTANCE;
    
    private Enclave enclave;
    
    public static EnclaveHolder instance(Enclave enclave) {
          INSTANCE.setEnclave(enclave);
          return INSTANCE;
    }

    private void setEnclave(Enclave enclave) {
        this.enclave = enclave;
    }

    public Enclave getEnclave() {
        return enclave;
    }

    
}
