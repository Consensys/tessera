package com.quorum.tessera.enclave.websockets;


public class EnclaveResponse<T> {
    
    private final EnclaveRequestType requestType;

    private final T payload;

    public EnclaveResponse(EnclaveRequestType requestType, T payload) {
        this.requestType = requestType;
        this.payload = payload;
    }

    public EnclaveRequestType getRequestType() {
        return requestType;
    }

    public T getPayload() {
        return payload;
    }

}
