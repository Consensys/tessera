package com.jpmorgan.quorum.enclave.websockets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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

        public Builder withArgs(Object... args) {
            this.args.addAll(Arrays.asList(args));
            return this;
        }

        public EnclaveRequest build() {
            return new EnclaveRequest(type, args);
        }

    }

}
