package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.PublicKey;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface PrivacyMetadata {

    PrivacyMode getPrivacyMode();

    List<AffectedTransaction> getAffectedContractTransactions();

    byte[] getExecHash();

    Optional<PublicKey> getPrivacyGroupId();

    class Builder {

        private PrivacyMode privacyMode;

        private List<AffectedTransaction> affectedTransactions = Collections.emptyList();

        private byte[] execHash = new byte[0];

        private PublicKey privacyGroupId;

        public static Builder create() {
            return new Builder();
        }

        public Builder withPrivacyMode(PrivacyMode privacyMode) {
            this.privacyMode = privacyMode;
            return this;
        }

        public Builder withAffectedTransactions(List<AffectedTransaction> affectedTransactions) {
            this.affectedTransactions = affectedTransactions;
            return this;
        }

        public Builder withExecHash(byte[] execHash) {
            this.execHash = execHash;
            return this;
        }

        public Builder withPrivacyGroupId(PublicKey privacyGroupId) {
            this.privacyGroupId = privacyGroupId;
            return this;
        }

        public PrivacyMetadata build() {

            Objects.requireNonNull(privacyMode, "privacyMode is required");

            if ((privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) == (execHash == null || execHash.length == 0)) {
                throw new RuntimeException("ExecutionHash data is invalid");
            }

            return new PrivacyMetadata() {
                @Override
                public PrivacyMode getPrivacyMode() {
                    return privacyMode;
                }

                @Override
                public List<AffectedTransaction> getAffectedContractTransactions() {
                    return affectedTransactions;
                }

                @Override
                public byte[] getExecHash() {
                    return execHash;
                }

                @Override
                public Optional<PublicKey> getPrivacyGroupId() {
                    return Optional.ofNullable(privacyGroupId);
                }
            };
        }

        public static Builder forStandardPrivate() {
            return create().withPrivacyMode(PrivacyMode.STANDARD_PRIVATE);
        }
    }
}
