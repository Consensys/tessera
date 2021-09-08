package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroup;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import java.util.*;

public interface SendSignedRequest {

  byte[] getSignedData();

  List<PublicKey> getRecipients();

  PrivacyMode getPrivacyMode();

  byte[] getExecHash();

  Set<MessageHash> getAffectedContractTransactions();

  Optional<PrivacyGroup.Id> getPrivacyGroupId();

  Set<PublicKey> getMandatoryRecipients();

  class Builder {

    private byte[] signedData;

    private List<PublicKey> recipients;

    private PrivacyMode privacyMode;

    private byte[] execHash = new byte[0];

    private Set<MessageHash> affectedContractTransactions = Collections.emptySet();

    private PrivacyGroup.Id privacyGroupId;

    private Set<PublicKey> mandatoryRecipients = Collections.emptySet();

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

    public Builder withAffectedContractTransactions(Set<MessageHash> affectedContractTransactions) {
      this.affectedContractTransactions = affectedContractTransactions;
      return this;
    }

    public Builder withExecHash(byte[] execHash) {
      this.execHash = execHash;
      return this;
    }

    public Builder withPrivacyMode(PrivacyMode privacyMode) {
      this.privacyMode = privacyMode;
      return this;
    }

    public Builder withPrivacyGroupId(PrivacyGroup.Id privacyGroupId) {
      this.privacyGroupId = privacyGroupId;
      return this;
    }

    public Builder withMandatoryRecipients(Set<PublicKey> mandatoryRecipients) {
      this.mandatoryRecipients = mandatoryRecipients;
      return this;
    }

    public SendSignedRequest build() {
      Objects.requireNonNull(signedData, "Signed data is required");
      Objects.requireNonNull(recipients, "recipients is required");
      Objects.requireNonNull(privacyMode, "privacyMode is required");

      if (privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
        if (execHash.length == 0) {
          throw new RuntimeException(
              "ExecutionHash is required for PRIVATE_STATE_VALIDATION privacy mode");
        }
      }

      if ((privacyMode == PrivacyMode.MANDATORY_RECIPIENTS) == mandatoryRecipients.isEmpty()) {
        throw new RuntimeException(
            "Mandatory recipients data only applicable for Mandatory Recipients privacy mode. "
                + "In case no mandatory recipient is required, consider using Party Protection privacy mode");
      }

      return new SendSignedRequest() {
        @Override
        public byte[] getSignedData() {
          return Arrays.copyOf(signedData, signedData.length);
        }

        @Override
        public List<PublicKey> getRecipients() {
          return List.copyOf(recipients);
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
        public Set<MessageHash> getAffectedContractTransactions() {
          return Set.copyOf(affectedContractTransactions);
        }

        @Override
        public Optional<PrivacyGroup.Id> getPrivacyGroupId() {
          return Optional.ofNullable(privacyGroupId);
        }

        @Override
        public Set<PublicKey> getMandatoryRecipients() {
          return Set.copyOf(mandatoryRecipients);
        }
      };
    }
  }
}
