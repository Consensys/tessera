package com.quorum.tessera.enclave.websockets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EnclaveRequest implements Serializable {
    
    private EnclaveRequestType type;

    private List<?> args;

    private EnclaveRequest(EnclaveRequestType type, List<?> args) {
        this.type = type;
        this.args = args;
    }

    public EnclaveRequestType getType() {
        return type;
    }

    public List<?> getArgs() {
        return args;
    }

    public static class Builder {

        private EnclaveRequestType type;

        private List<Object> args = new ArrayList<>();

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder withType(EnclaveRequestType type) {
            this.type = type;
            return this;
        }

        public <T> Builder withArg(T arg) {
            this.args.add(arg);
            return this;
        }
        
        public EnclaveRequest build() {

            if(type.getParamTypes().size() != args.size()) {
                throw new IllegalStateException("Param types and args are not of equal length:" + type + " "+ args);
            }
            
            return new EnclaveRequest(type, args);
        }

    }

}
