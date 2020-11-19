package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface PrivacyGroup {

    PublicKey getPrivacyGroupId();

    String getName();

    String getDescription();

    List<PublicKey> getMembers();

    byte[] getSeed();

    Type getType();

    State getState();

    enum Type {
        LEGACY,
        PANTHEON
    }

    enum State {
        ACTIVE,
        DELETED
    }

    class Builder {

        private PublicKey privacyGroupId;

        private String name = "";

        private String description = "";

        private List<PublicKey> members;

        private byte[] seed;

        private Type type;

        private State state;

        public static Builder create() {
            return new Builder() {};
        }

        public Builder withPrivacyGroupId(final PublicKey privacyGroupId) {
            this.privacyGroupId = privacyGroupId;
            return this;
        }

        public Builder withName(final String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }

        public Builder withMembers(final List<PublicKey> members) {
            this.members = members;
            return this;
        }

        public Builder withSeed(final byte[] seed) {
            this.seed = seed;
            return this;
        }

        public Builder withType(final Type type) {
            this.type = type;
            return this;
        }

        public Builder withState(final State state) {
            this.state = state;
            return this;
        }

        public PrivacyGroup build() {

            Objects.requireNonNull(privacyGroupId);
            Objects.requireNonNull(members);
            Objects.requireNonNull(type);
            Objects.requireNonNull(state);

            return new PrivacyGroup() {
                @Override
                public PublicKey getPrivacyGroupId() {
                    return privacyGroupId;
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getDescription() {
                    return description;
                }

                @Override
                public List<PublicKey> getMembers() {
                    return List.copyOf(members);
                }

                @Override
                public byte[] getSeed() {
                    return Arrays.copyOf(seed, seed.length);
                }

                @Override
                public Type getType() {
                    return type;
                }

                @Override
                public State getState() {
                    return state;
                }
            };
        }
    }
}
