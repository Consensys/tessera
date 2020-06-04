package com.quorum.tessera.transaction;

import com.quorum.tessera.encryption.PublicKey;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface SendSignedRequest {

    byte[] getSignedData();

    List<PublicKey> getRecipients();

    class Builder {

        private byte[] signedData;

        private List<PublicKey> recipients;

        public static Builder create() {
            return new Builder() {};
        }

        public Builder withSignedData(byte[] signedData) {
            this.signedData = signedData;
            return this;
        }

        public Builder withRecipients(List<PublicKey> recipients) {
            this.recipients = recipients;
            return this;
        }

        public SendSignedRequest build() {
            Objects.requireNonNull(signedData, "Signed data is required");
            Objects.requireNonNull(recipients, "recipients is required");

            return new SendSignedRequest() {
                @Override
                public byte[] getSignedData() {
                    return Arrays.copyOf(signedData, signedData.length);
                }

                @Override
                public List<PublicKey> getRecipients() {
                    return List.copyOf(recipients);
                }
            };
        }

    }

}
