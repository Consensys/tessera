package com.quorum.tessera.config.keys;

import com.quorum.tessera.argon2.ArgonOptions;
import java.util.Objects;

/**
 * Represents a full set of configuration options for
 * a key. This may or may not be an encrypted key, it
 * is upto the user of this object to decide based on what
 * properties are set.
 *
 * TODO: make copies of arrays on input and output
 */
public class KeyConfig {

    private final String value;

    private final String password;

    private final byte[] asalt;

    private final ArgonOptions argonOptions;

    private final byte[] sbox;

    private final byte[] snonce;

    private KeyConfig(final String value,
                      final String password,
                      final byte[] asalt,
                      final ArgonOptions argonOptions,
                      final byte[] sbox,
                      final byte[] snonce
    ) {
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

        public Builder sbox(final byte[] sbox) {
            this.sbox = sbox;
            return this;
        }

        public Builder snonce(final byte[] snonce) {
            this.snonce = snonce;
            return this;
        }

        public Builder asalt(final byte[] asalt) {
            this.asalt = asalt;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder value(final String value) {
            this.value = value;
            return this;
        }

        public Builder argonAlgorithm(final String argonAlgorithm) {
            this.argonAlgorithm = argonAlgorithm;
            return this;
        }

        public Builder argonIterations(final Integer argonIterations) {
            this.argonIterations = argonIterations;
            return this;
        }

        public Builder argonMemory(final Integer argonMemory) {
            this.argonMemory = argonMemory;
            return this;
        }

        public Builder argonParallelism(final Integer argonParallelism) {
            this.argonParallelism = argonParallelism;
            return this;
        }

        public Builder argonOptions(final ArgonOptions argonOptions) {
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
