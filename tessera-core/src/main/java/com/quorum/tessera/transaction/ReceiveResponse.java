package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyMode;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public interface ReceiveResponse {

    byte[] getUnencryptedTransactionData();

    PrivacyMode getPrivacyMode();

    byte[] getExecHash();

    Set<MessageHash> getAffectedTransactions();

    class Builder {

        private byte[] unencryptedTransactionData;

        private PrivacyMode privacyMode;

        private byte[] execHash;

        private Set<MessageHash> affectedTransactions = Set.of();

        private Builder() {
        }

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

        public ReceiveResponse build() {

            Objects.requireNonNull(privacyMode,"Privacy mode is required");
            Objects.requireNonNull(unencryptedTransactionData,"unencrypted payload is required");
            if(privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
                Objects.requireNonNull(execHash,"Exec hash is required if privacy mode is PRIVATE_STATE_VALIDATION");
            }
            return new ReceiveResponse() {

                @Override
                public byte[] getUnencryptedTransactionData() {
                    return Arrays.copyOf(unencryptedTransactionData,unencryptedTransactionData.length);
                }

                @Override
                public PrivacyMode getPrivacyMode() {
                    return privacyMode;
                }

                @Override
                public byte[] getExecHash() {
                    return Arrays.copyOf(execHash,execHash.length);
                }

                @Override
                public Set<MessageHash> getAffectedTransactions() {
                    return Set.copyOf(affectedTransactions);
                }
            };
        }
    }

}
