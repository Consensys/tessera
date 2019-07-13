package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.util.Objects;

public class SyncRequestMessage {

    public enum Type {
        TRANSACTION_PUSH,
        PARTY_INFO
    }

    private final EncodedPayload transactions;

    private final PartyInfo partyInfo;

    private final Type type;

    private SyncRequestMessage(Type type, PartyInfo partyInfo, EncodedPayload transactions) {
        this.partyInfo = partyInfo;
        this.type = type;
        this.transactions = transactions;
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

    public static class Builder {

        private final Type type;

        private PartyInfo partyInfo;

        private EncodedPayload transactions;

        private Builder(Type type) {
            this.type = type;
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
            if (type == Type.PARTY_INFO) {
                Objects.requireNonNull(partyInfo);
            }

            if (type == Type.TRANSACTION_PUSH) {
                Objects.requireNonNull(transactions);
            }
            return new SyncRequestMessage(type, partyInfo, transactions);
        }
    }
}
