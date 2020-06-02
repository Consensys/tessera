package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.PublicKey;

import java.util.Arrays;

public interface StoreRawRequest {

    byte[] getPayload();

    PublicKey getFrom();

    class Builder {

        private PublicKey sender;

        private byte[] payload;

        public static Builder create() {
            return new Builder() {};
        }

        public Builder withPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        public Builder withSender(PublicKey sender) {
            this.sender = sender;
            return this;
        }


        public StoreRawRequest build() {
            return new StoreRawRequest() {

                @Override
                public byte[] getPayload() {
                    return Arrays.copyOf(payload,payload.length);
                }

                @Override
                public PublicKey getFrom() {
                    return sender;
                }
            };
        }

    }

}
