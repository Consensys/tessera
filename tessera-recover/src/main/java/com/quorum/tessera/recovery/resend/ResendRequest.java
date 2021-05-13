package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Objects;

public interface ResendRequest {

  // FIXME: also defined in jaxrs module
  enum ResendRequestType {
    ALL,
    INDIVIDUAL
  }

  PublicKey getRecipient();

  ResendRequestType getType();

  MessageHash getHash();

  class Builder {

    private PublicKey recipient;

    private ResendRequestType type;

    private MessageHash hash;

    private Builder() {}

    public static Builder create() {
      return new Builder();
    }

    public Builder withRecipient(PublicKey recipient) {
      this.recipient = recipient;
      return this;
    }

    public Builder withHash(MessageHash hash) {
      this.hash = hash;
      return this;
    }

    public Builder withType(ResendRequestType type) {
      this.type = type;
      return this;
    }

    public ResendRequest build() {
      Objects.requireNonNull(recipient, "Recipient is required");
      Objects.requireNonNull(type, "ResendRequestType is required");
      if (type == ResendRequestType.INDIVIDUAL) {
        Objects.requireNonNull(hash, "Transaction hash is required for individual resends");
      }

      return new ResendRequest() {
        @Override
        public PublicKey getRecipient() {
          return recipient;
        }

        @Override
        public ResendRequestType getType() {
          return type;
        }

        @Override
        public MessageHash getHash() {
          return hash;
        }
      };
    }
  }
}
