package com.quorum.tessera.enclave;

import java.util.Objects;

public class AffectedTransaction {

    private final TxHash hash;

    private final EncodedPayload payload;

    private AffectedTransaction(TxHash hash, EncodedPayload payload) {
        this.hash = Objects.requireNonNull(hash);
        this.payload = Objects.requireNonNull(payload);
    }

    public TxHash getHash() {
        return hash;
    }


    public EncodedPayload getPayload() {
        return payload;
    }


    public static class Builder {

        private byte[] hash;

        private byte[] payload;

        public Builder withHash(byte[] hash) {
            this.hash = hash;
            return this;
        }

        public Builder withPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        private Builder() {}

        public static Builder create() {
            return new Builder();
        }

        public AffectedTransaction build() {
            final TxHash txHash = new TxHash(hash);
            final EncodedPayload encodedPayload = PayloadEncoder.create().decode(payload);
            return new AffectedTransaction(txHash,encodedPayload);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AffectedTransaction that = (AffectedTransaction) o;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
