package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;

import java.util.Objects;
import java.util.Set;

public interface SendResponse {

    MessageHash getTransactionHash();

    Set<PublicKey> getManagedParties();

    class Builder {

        private MessageHash messageHash;

        private Set<PublicKey> managedParties;

        private Builder() {}

        public static Builder create() {
            return new Builder();
        }

        public Builder withMessageHash(final MessageHash messageHash) {
            this.messageHash = messageHash;
            return this;
        }

        public Builder withManagedParties(final Set<PublicKey> managedKeys) {
            this.managedParties = managedKeys;
            return this;
        }

        public SendResponse build() {
            Objects.requireNonNull(messageHash, "Transaction hash is required");

            return new SendResponse() {

                @Override
                public MessageHash getTransactionHash() {
                    return messageHash;
                }

                @Override
                public Set<PublicKey> getManagedParties() {
                    return managedParties;
                }
            };
        }
    }
}
