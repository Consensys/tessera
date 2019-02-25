package com.quorum.tessera.enclave.websockets;

import java.util.Arrays;
import java.util.List;

public enum EnclaveRequestType {
    STATUS(EnclaveResponseType.STATUS),
    DEFAULT_PUBLIC_KEY(EnclaveResponseType.PUBLIC_KEY),
    FORWARDING_KEYS(EnclaveResponseType.PUBLIC_KEYS),
    PUBLIC_KEYS(EnclaveResponseType.PUBLIC_KEYS),
    ENCRYPT_PAYLOAD(EnclaveResponseType.ENCODED_PAYLOAD,ArgType.BYTE_ARRAY,ArgType.PUBLIC_KEY,ArgType.PUBLIC_KEY_LIST),
    ENCRYPT_RAWTXN_PAYLOAD(EnclaveResponseType.ENCODED_PAYLOAD,ArgType.RAW_TRANSACTION,ArgType.PUBLIC_KEY_LIST),
    ENCRYPT_RAW_PAYLOAD(EnclaveResponseType.RAW_TXN,ArgType.BYTE_ARRAY,ArgType.PUBLIC_KEY),
    UNENCRYPT_TXN(EnclaveResponseType.BYTES,ArgType.ENCODED_PAYLOAD,ArgType.PUBLIC_KEY),
    CREATE_NEW_RECIPIENT_BOX(EnclaveResponseType.BYTES,ArgType.ENCODED_PAYLOAD,ArgType.PUBLIC_KEY);
    

    private EnclaveResponseType responseType;
    
    private final List<ArgType> paramTypes;
    
    EnclaveRequestType(EnclaveResponseType responseType,ArgType... paramTypes) {
        this.responseType = responseType;
        this.paramTypes = Arrays.asList(paramTypes);
    }

    public EnclaveResponseType getResponseType() {
        return responseType;
    }

    
    
    public List<ArgType> getParamTypes() {
        return paramTypes;
    }



}
