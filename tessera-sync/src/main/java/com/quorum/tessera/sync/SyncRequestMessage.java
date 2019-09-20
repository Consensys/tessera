package com.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.util.Objects;
import java.util.UUID;

public class SyncRequestMessage {

    public enum Type {
        TRANSACTION_PUSH,
        PARTY_INFO,
        TRANSACTION_SYNC
    }

    private final String correlationId;

    private final PublicKey recipientKey;

    private final EncodedPayload transactions;

    private final PartyInfo partyInfo;

    private final Type type;

    private SyncRequestMessage(Type type, PartyInfo partyInfo, EncodedPayload transactions, PublicKey recipientKey) {
        this.partyInfo = partyInfo;
        this.type = type;
        this.transactions = transactions;
        this.recipientKey = recipientKey;
        this.correlationId = UUID.randomUUID().toString();
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    public EncodedPayload getTransactions() {
        return transactions;
    }

    public Type getType() {
        return type;
    }

    public PublicKey getRecipientKey() {
        return recipientKey;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public static class Builder {

        private final Type type;

        private PartyInfo partyInfo;

        private EncodedPayload transactions;

        private PublicKey recipientKey;

        private Builder(Type type) {
            this.type = type;
        }

        public Builder withRecipientKey(PublicKey recipientKey) {
            this.recipientKey = recipientKey;
            return this;
        }

        public Builder withPartyInfo(PartyInfo partyInfo) {
            this.partyInfo = partyInfo;
            return this;
        }

        public Builder withTransactions(EncodedPayload transactions) {
            this.transactions = transactions;
            return this;
        }

        public static Builder create(Type type) {
            return new Builder(type);
        }

        public SyncRequestMessage build() {
            Objects.requireNonNull(type);
            if (type == Type.TRANSACTION_PUSH) {
                Objects.requireNonNull(transactions);
            }

            if (type == Type.TRANSACTION_SYNC) {
                Objects.requireNonNull(recipientKey);
            }
            return new SyncRequestMessage(type, partyInfo, transactions, recipientKey);
        }
    }
}
