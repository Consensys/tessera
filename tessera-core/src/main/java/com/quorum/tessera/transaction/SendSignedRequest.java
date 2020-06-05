package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface SendSignedRequest {

    byte[] getSignedData();

    List<PublicKey> getRecipients();

    PrivacyMode getPrivacyMode();

    byte[] getExecHash();

    Set<MessageHash> getAffectedContractTransactions();

    class Builder {

        private byte[] signedData;

        private List<PublicKey> recipients;

        private PublicKey from;

        private PrivacyMode privacyMode;

        private byte[] execHash;

        private Set<MessageHash> affectedContractTransactions;

        public static Builder create() {
            return new Builder() {};
        }

        public Builder withSignedData(byte[] signedData) {
            this.signedData = signedData;
            return this;
        }

//        public Builder withSender(PublicKey from) {
//            this.from = from;
//            return this;
//        }

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
        public SendSignedRequest build() {
            Objects.requireNonNull(signedData, "Signed data is required");
            Objects.requireNonNull(recipients, "recipients is required");
            Objects.requireNonNull(privacyMode,"privacyMode is required");

            if(privacyMode == PrivacyMode.PRIVATE_STATE_VALIDATION) {
                Objects.requireNonNull(execHash,"execHash is required for PRIVATE_STATE_VALIDATION privacy mode");
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
                    return execHash;
                }

                @Override
                public Set<MessageHash> getAffectedContractTransactions() {
                    return Set.copyOf(affectedContractTransactions);
                }
            };
        }

    }

}
