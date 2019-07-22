package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.util.Objects;

public class SyncResponseMessage {

    public enum Type {
        PARTY_INFO,
        TRANSACTION_SYNC;
    }

    private final Type type;

    private final PartyInfo partyInfo;

    private final Long transactionOffset;

    private final Long transactionCount;

    private final EncodedPayload transactions;

    private SyncResponseMessage(
            Type type,
            PartyInfo partyInfo,
            Long transactionOffset,
            Long transactionCount,
            EncodedPayload transactions) {
        this.type = type;
        this.partyInfo = partyInfo;
        this.transactionOffset = transactionOffset;
        this.transactionCount = transactionCount;
        this.transactions = transactions;
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    public Long getTransactionOffset() {
        return transactionOffset;
    }

    public Long getTransactionCount() {
        return transactionCount;
    }

    public EncodedPayload getTransactions() {
        return transactions;
    }

    public Type getType() {
        return type;
    }

    public static class Builder {

        private Type type;

        private PartyInfo partyInfo;

        private Long transactionOffset;

        private Long transactionCount;

        private EncodedPayload transactions;

        private Builder(Type type) {
            this.type = type;
        }

        public static Builder create(Type type) {
            return new Builder(type);
        }

        public SyncResponseMessage build() {
            Objects.requireNonNull(type);

            if (type == Type.PARTY_INFO) {
                Objects.requireNonNull(partyInfo);
                return new SyncResponseMessage(type, partyInfo, null, null, null);
            }

            Objects.requireNonNull(transactionOffset);
            Objects.requireNonNull(transactionCount);
            Objects.requireNonNull(transactions);
            // TODO: validate counts/offsets and list size
            return new SyncResponseMessage(type, null, transactionOffset, transactionCount, transactions);
        }

        public Builder withPartyInfo(PartyInfo partyInfo) {
            this.partyInfo = partyInfo;
            return this;
        }

        public Builder withTransactions(EncodedPayload transactions) {
            this.transactions = transactions;
            return this;
        }

        public Builder withTransactionCount(long transactionCount) {
            this.transactionCount = transactionCount;
            return this;
        }

        public Builder withTransactionOffset(long transactionOffset) {
            this.transactionOffset = transactionOffset;
            return this;
        }
    }
}
