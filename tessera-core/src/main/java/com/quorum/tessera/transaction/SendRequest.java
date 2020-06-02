package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.PublicKey;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface SendRequest {

    PublicKey getSender();

    List<PublicKey> getRecipients();

    byte[] getPayload();


    class Builder {

        private PublicKey from;

        private List<PublicKey> recipients;

        private byte[] payload;

        public static Builder create() {
            return new Builder() {};
        }

        public Builder withSender(PublicKey from) {
            this.from = from;
            return this;
        }

        public Builder withRecipients(List<PublicKey> recipients) {
            this.recipients = recipients;
            return this;
        }

        public Builder withPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        public SendRequest build() {

            Objects.requireNonNull(from,"Sender is required");
            Objects.requireNonNull(recipients,"Recipents are required");
            Objects.requireNonNull(payload,"Payload is required");

            return new SendRequest() {

                @Override
                public PublicKey getSender() {
                    return from;
                }

                @Override
                public List<PublicKey> getRecipients() {
                    return List.copyOf(recipients);
                }

                @Override
                public byte[] getPayload() {
                    return Arrays.copyOf(payload,payload.length);
                }
            };
        }


    }

}
