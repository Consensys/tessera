package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.enclave.EncodedPayload;

public interface ResendResponse {

  EncodedPayload getPayload();

  class Builder {

    private EncodedPayload payload;

    private Builder() {}

    public static Builder create() {
      return new Builder();
    }

    public Builder withPayload(EncodedPayload payload) {
      this.payload = payload;
      return this;
    }

    public ResendResponse build() {
      return new ResendResponse() {
        @Override
        public EncodedPayload getPayload() {
          return payload;
        }
      };
    }
  }
}
