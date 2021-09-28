package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Arrays;
import java.util.Objects;

public interface StoreRawRequest {

  byte[] getPayload();

  PublicKey getSender();

  EncodedPayloadCodec getEncodedPayloadCodec();

  class Builder {

    private PublicKey sender;

    private byte[] payload;

    private EncodedPayloadCodec encodedPayloadCodec;

    public static Builder create() {
      return new Builder() {};
    }

    public Builder withPayload(byte[] payload) {
      this.payload = payload;
      return this;
    }

    public Builder withEncodedPayloadCodec(EncodedPayloadCodec encodedPayloadCodec) {
      this.encodedPayloadCodec = encodedPayloadCodec;
      return this;
    }

    public Builder withSender(PublicKey sender) {
      this.sender = sender;
      return this;
    }

    public StoreRawRequest build() {
      Objects.requireNonNull(payload, "Payload is required");
      Objects.requireNonNull(sender, "Sender is required");
      Objects.requireNonNull(encodedPayloadCodec, "EncodedPayloadCodec is required");

      return new StoreRawRequest() {

        @Override
        public byte[] getPayload() {
          return Arrays.copyOf(payload, payload.length);
        }

        @Override
        public PublicKey getSender() {
          return sender;
        }

        @Override
        public EncodedPayloadCodec getEncodedPayloadCodec() {
          return encodedPayloadCodec;
        }
      };
    }
  }
}
