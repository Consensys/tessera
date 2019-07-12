package com.jpmorgan.quorum.tessera.sync;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import java.util.Objects;

public class SyncResponseMessage {

    private final PartyInfo partyInfo;

    private final long transactionOffset;

    private final long transactionCount;

    private final EncodedPayload transactions;

    public SyncResponseMessage(PartyInfo partyInfo, long transactionOffset, long transactionCount, EncodedPayload transactions) {
        this.partyInfo = partyInfo;
        this.transactionOffset = transactionOffset;
        this.transactionCount = transactionCount;
        this.transactions = transactions;
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    public long getTransactionOffset() {
        return transactionOffset;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public EncodedPayload getTransactions() {
        return transactions;
    }

    public static class Builder {

        private PartyInfo partyInfo;

        private Long transactionOffset;

        private Long transactionCount;

        private EncodedPayload transactions;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public SyncResponseMessage build() {
            
            Objects.requireNonNull(partyInfo);
            Objects.requireNonNull(transactionOffset);
            Objects.requireNonNull(transactionCount);
            //TODO: validate counts/offsets and list size
            return new SyncResponseMessage(partyInfo, transactionOffset, transactionCount, transactions);
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
