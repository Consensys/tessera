package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Objects;
import java.util.Optional;

public interface ReceiveRequest {

  MessageHash getTransactionHash();

  Optional<PublicKey> getRecipient();

  boolean isRaw();

  class Builder {

    private MessageHash messageHash;

    private PublicKey recipient;

    private boolean raw;

    public Builder withRaw(boolean raw) {
      this.raw = raw;
      return this;
    }

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
      Objects.requireNonNull(messageHash, "Message hash is required");

      return new ReceiveRequest() {
        @Override
        public MessageHash getTransactionHash() {
          return messageHash;
        }

        @Override
        public Optional<PublicKey> getRecipient() {
          return Optional.ofNullable(recipient);
        }

        @Override
        public boolean isRaw() {
          return raw;
        }
      };
    }
  }
}
