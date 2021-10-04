package com.quorum.tessera.recovery.resend;

import java.util.Objects;
import java.util.Optional;

public interface ResendBatchRequest {

  String getPublicKey();

  Optional<Integer> getBatchSize();

  class Builder {

    private String publicKey;

    private Integer batchSize;

    public static Builder create() {
      return new Builder() {};
    }

    public Builder withPublicKey(String publicKey) {
      this.publicKey = publicKey;
      return this;
    }

    public Builder withBatchSize(Integer batchSize) {
      this.batchSize = batchSize;
      return this;
    }

    public ResendBatchRequest build() {

      Objects.requireNonNull(publicKey, "publicKey is required");

      if (Objects.nonNull(batchSize) && batchSize <= 0) {
        throw new IllegalArgumentException("Batch size must be greater than 1");
      }

      return new ResendBatchRequest() {

        @Override
        public String getPublicKey() {
          return publicKey;
        }

        @Override
        public Optional<Integer> getBatchSize() {
          return Optional.ofNullable(batchSize);
        }

        @Override
        public String toString() {
          return ResendBatchRequest.class.getSimpleName()
              + "[publicKey:"
              + publicKey
              + ",batchSize"
              + batchSize
              + "]";
        }
      };
    }
  }
}
