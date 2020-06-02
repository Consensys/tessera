package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;

import java.util.Optional;

public interface ReceiveRequest {

    MessageHash getTransactionHash();

    Optional<PublicKey> getRecipient();


    class Builder {

        private MessageHash messageHash;

        private PublicKey recipient;

        public Builder withRecipient(PublicKey recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder withTransactionHash(MessageHash messageHash) {
            this.messageHash = messageHash;
            return this;
        }


        public static Builder create() {
            return new Builder() {};
        }

        public ReceiveRequest build() {
            return new ReceiveRequest() {
                @Override
                public MessageHash getTransactionHash() {
                    return messageHash;
                }

                @Override
                public Optional<PublicKey> getRecipient() {
                    return Optional.ofNullable(recipient);
                }
            };
        }


    }


}
