package com.github.nexus.keyenc;

import com.github.nexus.argon2.ArgonOptions;
import java.util.Objects;

public class KeyConfig {

    private final String value;

    private final String password;

    private final byte[] asalt;

    private final ArgonOptions argonOptions;

    private final byte[] sbox;

    private final byte[] snonce;

    private KeyConfig(String value, String password, byte[] asalt, ArgonOptions argonOptions, byte[] sbox, byte[] snonce) {
        this.value = value;
        this.password = password;
        this.asalt = asalt;
        this.argonOptions = argonOptions;
        this.sbox = sbox;
        this.snonce = snonce;
    }

    public String getValue() {
        return value;
    }

    public String getPassword() {
        return password;
    }

    public byte[] getAsalt() {
        return asalt;
    }

    public ArgonOptions getArgonOptions() {
        return argonOptions;
    }

    public byte[] getSbox() {
        return sbox;
    }

    public byte[] getSnonce() {
        return snonce;
    }

    public static class Builder {

        private String value;

        private String password;

        private byte[] asalt;

        private String argonAlgorithm;

        private Integer argonIterations;

        private Integer argonMemory;

        private Integer argonParallelism;

        private byte[] sbox;

        private byte[] snonce;

        private ArgonOptions argonOptions;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder sbox(byte[] sbox) {
            this.sbox = sbox;
            return this;
        }

        public Builder snonce(byte[] snonce) {
            this.snonce = snonce;
            return this;
        }

        public Builder asalt(byte[] asalt) {
            this.asalt = asalt;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder argonAlgorithm(String argonAlgorithm) {
            this.argonAlgorithm = argonAlgorithm;
            return this;
        }

        public Builder argonIterations(Integer argonIterations) {
            this.argonIterations = argonIterations;
            return this;
        }

        public Builder argonMemory(Integer argonMemory) {
            this.argonMemory = argonMemory;
            return this;
        }

        public Builder argonParallelism(Integer argonParallelism) {
            this.argonParallelism = argonParallelism;
            return this;
        }

        public Builder argonOptions(ArgonOptions argonOptions) {
            this.argonOptions = argonOptions;
            return this;
        }

        public KeyConfig build() {

            final ArgonOptions argonOpts;
            if (Objects.isNull(argonOptions)) {
                argonOpts = new ArgonOptions(argonAlgorithm, argonIterations, argonMemory, argonParallelism);
            } else {
                argonOpts = argonOptions;
            }

            return new KeyConfig(value, password, asalt, argonOpts, sbox, snonce);
        }

    }

}
