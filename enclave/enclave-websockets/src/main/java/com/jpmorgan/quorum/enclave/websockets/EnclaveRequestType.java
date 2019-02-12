package com.jpmorgan.quorum.enclave.websockets;

import java.util.Arrays;
import java.util.List;

public enum EnclaveRequestType {
    
    DEFAULT_PUBLIC_KEY,
    FORWARDING_KEYS,
    PUBLIC_KEYS,
    ENCRYPT_PAYLOAD(ArgType.BYTE_ARRAY,ArgType.PUBLIC_KEY,ArgType.PUBLIC_KEY_LIST),
    ENCRYPT_RAWTXN_PAYLOAD(ArgType.RAW_TRANSACTION,ArgType.PUBLIC_KEY_LIST),
    ENCRYPT_RAW_PAYLOAD(ArgType.BYTE_ARRAY,ArgType.PUBLIC_KEY),
    UNENCRYPT_TXN(ArgType.ENCODED_PAYLOAD,ArgType.PUBLIC_KEY),
    CREATE_NEW_RECIPIENT_BOX(ArgType.ENCODED_PAYLOAD,ArgType.PUBLIC_KEY);
    

    private final List<ArgType> paramTypes;
    
    EnclaveRequestType(ArgType... paramTypes) {
        this.paramTypes = Arrays.asList(paramTypes);
    }

    public List<ArgType> getParamTypes() {
        return paramTypes;
    }



}
