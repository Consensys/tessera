package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;

public interface ReceiveResponse {

  byte[] getUnencryptedTransactionData();

  PrivacyMode getPrivacyMode();

  byte[] getExecHash();

  Set<MessageHash> getAffectedTransactions();

  Set<PublicKey> getManagedParties();

  PublicKey sender();

  Optional<PrivacyGroup.Id> getPrivacyGroupId();

  class Builder {

    private byte[] unencryptedTransactionData;

    private PrivacyMode privacyMode;

    private byte[] execHash = new byte[0];

    private Set<MessageHash> affectedTransactions = Collections.emptySet();

    private Set<PublicKey> managedParties = Collections.emptySet();

    private PublicKey sender;

    private PrivacyGroup.Id privacyGroupId;

    private Builder() {}

    public static Builder create() {
      return new Builder();
    }

    public Builder withUnencryptedTransactionData(byte[] unencryptedTransactionData) {
      this.unencryptedTransactionData = unencryptedTransactionData;
      return this;
    }

    public Builder withPrivacyMode(PrivacyMode privacyMode) {
      this.privacyMode = privacyMode;
      return this;
    }

    public Builder withExecHash(byte[] execHash) {
      this.execHash = execHash;
      return this;
    }

    public Builder withAffectedTransactions(Set<MessageHash> affectedTransactions) {
      this.affectedTransactions = affectedTransactions;
      return this;
    }

    public Builder withManagedParties(Set<PublicKey> managedKeys) {
      this.managedParties = managedKeys;
      return this;
    }

    public Builder withSender(PublicKey sender) {
      this.sender = sender;
      return this;
    }

    public Builder withPrivacyGroupId(PrivacyGroup.Id privacyGroupId) {
      this.privacyGroupId = privacyGroupId;
      return this;
    }

    public ReceiveResponse build() {

      Objects.requireNonNull(unencryptedTransactionData, "unencrypted payload is required");
      Objects.requireNonNull(privacyMode, "Privacy mode is required");
      Objects.requireNonNull(sender, "transaction sender is required");

      if (privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
        if (execHash.length == 0) {
          throw new RuntimeException(
              "ExecutionHash is required for PRIVATE_STATE_VALIDATION privacy mode");
        }
      }

      return new ReceiveResponse() {

        @Override
        public byte[] getUnencryptedTransactionData() {
          return Arrays.copyOf(unencryptedTransactionData, unencryptedTransactionData.length);
        }

        @Override
        public PrivacyMode getPrivacyMode() {
          return privacyMode;
        }

        @Override
        public byte[] getExecHash() {
          return Arrays.copyOf(execHash, execHash.length);
        }

        @Override
        public Set<MessageHash> getAffectedTransactions() {
          return Set.copyOf(affectedTransactions);
        }

        @Override
        public Set<PublicKey> getManagedParties() {
          return managedParties;
        }

        @Override
        public PublicKey sender() {
          return sender;
        }

        @Override
        public Optional<PrivacyGroup.Id> getPrivacyGroupId() {
          return Optional.ofNullable(privacyGroupId);
        }
      };
    }
  }
}
