package com.jpmorgan.quorum.enclave.websockets;

import java.util.Arrays;
import java.util.List;

public enum EnclaveRequestType {
    DEFAULT_PUBLIC_KEY,
    FORWARDING_KEYS,
    PUBLIC_KEYS,
    ENCRYPT_PAYLOAD(ArgType.BYTE_ARRAY,ArgType.PUBLIC_KEY,ArgType.PUBLIC_KEY_LIST);

    private final List<ArgType> paramTypes;
    
    EnclaveRequestType(ArgType... paramTypes) {
        this.paramTypes = Arrays.asList(paramTypes);
    }

    public List<ArgType> getParamTypes() {
        return paramTypes;
    }



}
