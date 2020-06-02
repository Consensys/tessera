package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.PublicKey;

import java.util.Arrays;
import java.util.List;

public interface SendRequest {

    PublicKey getFrom();

    List<PublicKey> getTo();

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
            return new SendRequest() {

                @Override
                public PublicKey getFrom() {
                    return from;
                }

                @Override
                public List<PublicKey> getTo() {
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
