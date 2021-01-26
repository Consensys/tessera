package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyGroupId;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;

import java.util.*;

public interface SendSignedRequest {

    byte[] getSignedData();

    List<PublicKey> getRecipients();

    PrivacyMode getPrivacyMode();

    byte[] getExecHash();

    Set<MessageHash> getAffectedContractTransactions();

    Optional<PrivacyGroupId> getPrivacyGroupId();

    class Builder {

        private byte[] signedData;

        private List<PublicKey> recipients;

        private PublicKey from;

        private PrivacyMode privacyMode;

        private byte[] execHash;

        private Set<MessageHash> affectedContractTransactions;

        private PrivacyGroupId privacyGroupId;

        public static Builder create() {
            return new Builder() {};
        }

        public Builder withSignedData(byte[] signedData) {
            this.signedData = signedData;
            return this;
        }

        public Builder withSender(PublicKey from) {
            this.from = from;
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

        public Builder withPrivacyGroupId(PrivacyGroupId privacyGroupId) {
            this.privacyGroupId = privacyGroupId;
            return this;
        }

        public SendSignedRequest build() {
            Objects.requireNonNull(signedData, "Signed data is required");
            Objects.requireNonNull(recipients, "recipients is required");
            Objects.requireNonNull(privacyMode, "privacyMode is required");
            Objects.requireNonNull(affectedContractTransactions, "affectedContractTransactions is required");
            Objects.requireNonNull(execHash, "ExecutionHash is required");

            if (privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
                if (execHash.length == 0) {
                    throw new RuntimeException("ExecutionHash is required for PRIVATE_STATE_VALIDATION privacy mode");
                }
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
                public Optional<PrivacyGroupId> getPrivacyGroupId() {
                    return Optional.ofNullable(privacyGroupId);
                }
            };
        }
    }
}
