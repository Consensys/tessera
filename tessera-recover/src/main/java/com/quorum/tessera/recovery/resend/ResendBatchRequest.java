package com.quorum.tessera.recovery.resend;

import java.util.Objects;

public interface ResendBatchRequest {

  String getPublicKey();

  int getBatchSize();

  class Builder {

    private String publicKey;

    private int batchSize;

    public static Builder create() {
      return new Builder() {};
    }

    public Builder withPublicKey(String publicKey) {
      this.publicKey = publicKey;
      return this;
    }

    public Builder withBatchSize(int batchSize) {
      this.batchSize = batchSize;
      return this;
    }

    public ResendBatchRequest build() {

      Objects.requireNonNull(publicKey, "publicKey is required");

      return new ResendBatchRequest() {

        @Override
        public String getPublicKey() {
          return publicKey;
        }

        @Override
        public int getBatchSize() {
          return batchSize;
        }
      };
    }
  }
}
