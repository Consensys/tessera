package com.jpmorgan.quorum.enclave.websockets;

import java.io.Serializable;


public class EnclaveResponse<T> implements Serializable {
    
    private EnclaveRequestType type;
    
    private T result;

    private EnclaveResponse(EnclaveRequestType type, T result) {
        this.type = type;
        this.result = result;
    }

    public EnclaveRequestType getType() {
        return type;
    }

    public T getResult() {
        return result;
    }
    
    public static class Builder {

        private EnclaveRequestType type;
        
        private Object result;

        private Builder() {
        }

        public Builder withType(EnclaveRequestType type) {
            this.type = type;
            return this;
        }
        
        public Builder withResult(Object result) {
            this.result = result;
            return this;
        }
        
        public static Builder create() {
            return new Builder();
        }
        
        public EnclaveResponse build() {
            return new EnclaveResponse(type,result);
        }
        
        
    }
    
    
}
